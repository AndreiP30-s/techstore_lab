package com.example.techstore.lab2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.dto.ProductRequest;
import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.catalog.Category;
import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.Characteristic;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.ProductSpecification;
import com.example.techstore.service.ProductService;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(OutputCaptureExtension.class)
@WithMockUser(roles = "ADMIN")
class ProductApiTest extends DatabaseFreeTest {
    @Autowired ProductService service;
    @Autowired MockMvc mvc;
    static final String JSON =
            """
{"name":"Test headphones","brand":"DemoBrand","category":"HEADPHONES","price":5000,
 "available":true,"specifications":{"headphoneType":"OVER_EAR","wireless":"true",
 "noiseCancelling":"false","minFrequencyHz":"20","maxFrequencyHz":"20000"},"rearCameras":[]}
""";

    ProductRequest request() {
        return new ProductRequest(
                "Test headphones",
                "DemoBrand",
                ProductCategory.HEADPHONES,
                new BigDecimal("5000.00"),
                true,
                Map.of(
                        "headphoneType",
                        "OVER_EAR",
                        "wireless",
                        "true",
                        "noiseCancelling",
                        "false",
                        "minFrequencyHz",
                        "20",
                        "maxFrequencyHz",
                        "20000"),
                List.of());
    }

    Product entity(int article) {
        Product entity = new Product();
        entity.setArticle(article);
        entity.setName("Test headphones");
        entity.setPrice(new BigDecimal("5000.00"));
        entity.setBrand(new Brand("DemoBrand"));
        Category category = new Category();
        category.setCode("HEADPHONES");
        category.setName("Наушники");
        entity.setCategory(category);
        request()
                .specifications()
                .forEach(
                        (key, value) ->
                                entity.getSpecifications()
                                        .add(new ProductSpecification(entity, key, value)));
        return entity;
    }

    @BeforeEach
    void prepare() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(false);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        when(brands.findById("DemoBrand")).thenReturn(Optional.of(new Brand("DemoBrand")));
        Category category = new Category();
        category.setCode("HEADPHONES");
        category.setName("Наушники");
        when(categories.findById("HEADPHONES")).thenReturn(Optional.of(category));
        when(links.findByIdCategoryCode("HEADPHONES"))
                .thenReturn(
                        request().specifications().keySet().stream()
                                .map(key -> new CategoryCharacteristic("HEADPHONES", key))
                                .toList());
        request()
                .specifications()
                .forEach(
                        (key, value) -> {
                            Characteristic spec = new Characteristic();
                            spec.setName(key);
                            spec.setValueType(
                                    key.equals("headphoneType")
                                            ? "ENUM"
                                            : key.endsWith("Hz") ? "INTEGER" : "BOOLEAN");
                            when(specifications.findById(key)).thenReturn(Optional.of(spec));
                        });
        when(products.saveAndFlush(any()))
                .thenAnswer(
                        invocation -> {
                            Product entity = invocation.getArgument(0);
                            entity.setArticle(100001);
                            return entity;
                        });
    }

    @Test
    void createsProductAndChildKeys(CapturedOutput output) throws Exception {
        mvc.perform(post("/api/products").contentType("application/json").content(JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/100001"))
                .andExpect(jsonPath("$.specifications.ramGb").doesNotExist())
                .andExpect(jsonPath("$.specifications.wireless").value("true"));
        ArgumentCaptor<Product> saved = ArgumentCaptor.forClass(Product.class);
        verify(products).saveAndFlush(saved.capture());
        assertEquals(5, saved.getValue().getSpecifications().size());
        assertThat(output).contains("Создан товар: артикул=100001, название=Test headphones");
        assertTrue(
                saved.getValue().getSpecifications().stream()
                        .allMatch(v -> v.getId().getProductArticle() == 100001));
    }

    @Test
    void readsAndUpdatesProductWithoutLosingCharacteristics() throws Exception {
        Product entity = entity(42);
        when(products.findById(42)).thenReturn(Optional.of(entity));
        mvc.perform(get("/api/products/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.article").value(42));
        mvc.perform(
                        put("/api/products/42")
                                .contentType("application/json")
                                .content(JSON.replace("5000", "6000")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(6000));
        assertEquals(5, entity.getSpecifications().size());
    }

    @Test
    void deletesProduct() throws Exception {
        Product entity = entity(42);
        when(products.findById(42)).thenReturn(Optional.of(entity));
        mvc.perform(delete("/api/products/42")).andExpect(status().isNoContent());
        verify(products).delete(entity);
    }

    @Test
    void reportsMissingAndInvalidRequests() throws Exception {
        mvc.perform(get("/api/products/999")).andExpect(status().isNotFound());
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(JSON.replace("5000", "-1")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/products").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products?minPrice=100&maxPrice=10"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/products?size=101")).andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(
                                        JSON.replace(
                                                "\"rearCameras\":[]", "\"rearCameras\":[null]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsWrongTypesUnknownCharacteristicsAndIncompleteModels() throws Exception {
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(JSON.replace("\"true\"", "\"yes\"")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(JSON.replace("minFrequencyHz", "unknown")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(JSON.replace("\"minFrequencyHz\":\"20\",", "")))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products")
                                .contentType("application/json")
                                .content(JSON.replace("OVER_EAR", "WRONG")))
                .andExpect(status().isBadRequest());
    }

    @SuppressWarnings("unchecked")
    @Test
    void paginatesUsingDatabaseQuery() throws Exception {
        when(products.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity(42)), PageRequest.of(0, 10), 1));
        mvc.perform(
                        get(
                                "/api/products?name=test&brand=DemoBrand&category=HEADPHONES&minPrice=1&maxPrice=6000&available=true&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].article").value(42));
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(products).findAll(any(Specification.class), pageable.capture());
        assertEquals(10, pageable.getValue().getPageSize());
    }

    @Test
    void blocksCatalogueChangesDuringDiscount() throws Exception {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(true);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        mvc.perform(post("/api/products").contentType("application/json").content(JSON))
                .andExpect(status().isConflict());
        verify(products, never()).saveAndFlush(any());
    }

    @Test
    void hasNoDataSourceOrEntityManager(
            @Autowired org.springframework.context.ApplicationContext context) {
        assertTrue(context.getBeansOfType(javax.sql.DataSource.class).isEmpty());
        assertTrue(
                context.getBeansOfType(jakarta.persistence.EntityManagerFactory.class).isEmpty());
    }

    @Test
    void rejectsNullOptionalCharacteristicAtHttpBoundary() throws Exception {
        String payload =
                JSON.replace("\"wireless\":\"true\"", "\"wireless\":\"true\",\"color\":null");
        mvc.perform(post("/api/products").contentType("application/json").content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
        verify(products, never()).saveAndFlush(any());
    }
}
