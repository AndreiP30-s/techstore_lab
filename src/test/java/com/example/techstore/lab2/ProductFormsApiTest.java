package com.example.techstore.lab2;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.model.catalog.*;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.*;
import com.example.techstore.model.user.*;
import com.example.techstore.service.ProductRules;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

@WithMockUser(roles = "ADMIN")
class ProductFormsApiTest extends DatabaseFreeTest {
    @Autowired MockMvc mvc;

    @BeforeEach
    void prepare() {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(false);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        when(brands.findById(anyString()))
                .thenAnswer(i -> Optional.of(new Brand(i.getArgument(0))));
        when(categories.findById(anyString()))
                .thenAnswer(
                        i -> {
                            Category category = new Category();
                            category.setCode(i.getArgument(0));
                            category.setName(i.getArgument(0));
                            return Optional.of(category);
                        });
        when(links.findByIdCategoryCode(anyString()))
                .thenAnswer(
                        i -> {
                            String category = i.getArgument(0);
                            return ProductRules.required(ProductCategory.valueOf(category)).stream()
                                    .map(key -> new CategoryCharacteristic(category, key))
                                    .toList();
                        });
        when(specifications.findById(anyString()))
                .thenAnswer(
                        i -> {
                            String key = i.getArgument(0);
                            Characteristic characteristic = new Characteristic();
                            characteristic.setName(key);
                            characteristic.setValueType(
                                    Set.of("wireless", "noiseCancelling", "supports5g", "cellular")
                                                    .contains(key)
                                            ? "BOOLEAN"
                                            : Set.of(
                                                                    "ramGb",
                                                                    "storageGb",
                                                                    "batteryCapacityMah",
                                                                    "minFrequencyHz",
                                                                    "maxFrequencyHz")
                                                            .contains(key)
                                                    ? "INTEGER"
                                                    : Set.of(
                                                                            "screenDiagonalInches",
                                                                            "frontCameraMp")
                                                                    .contains(key)
                                                            ? "DECIMAL"
                                                            : Set.of("storageType", "headphoneType")
                                                                            .contains(key)
                                                                    ? "ENUM"
                                                                    : "TEXT");
                            return Optional.of(characteristic);
                        });
        when(products.saveAndFlush(any()))
                .thenAnswer(
                        i -> {
                            Product product = i.getArgument(0);
                            product.setArticle(73);
                            return product;
                        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"smartphones", "tablets", "laptops", "headphones"})
    void createsEachCategoryFromTwoRequiredFields(String category) throws Exception {
        String expected =
                switch (category) {
                    case "smartphones" -> "SMARTPHONE";
                    case "tablets" -> "TABLET";
                    case "laptops" -> "LAPTOP";
                    default -> "HEADPHONES";
                };
        mvc.perform(
                        post("/api/products/" + category)
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "New product")
                                .param("price", "12345.67"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/products/73"))
                .andExpect(jsonPath("$.category").value(expected))
                .andExpect(jsonPath("$.price").value(12345.67))
                .andExpect(jsonPath("$.brand").value("DemoBrand"));
        verify(products).saveAndFlush(any());
    }

    @Test
    void automaticallyCreatesNewBrand() throws Exception {
        mvc.perform(
                        post("/api/products/smartphones")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "New phone")
                                .param("price", "100")
                                .param("brand", "New brand"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.brand").value("New brand"));
        ArgumentCaptor<Brand> saved = ArgumentCaptor.forClass(Brand.class);
        verify(brands).saveAndFlush(saved.capture());
        assertEquals("New brand", saved.getValue().getName());
    }

    @Test
    void validatesFormFieldsAndBindingErrors() throws Exception {
        mvc.perform(
                        post("/api/products/smartphones")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("price", "100"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products/smartphones")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "Phone")
                                .param("price", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details").isArray());
        mvc.perform(
                        post("/api/products/smartphones")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "Phone")
                                .param("price", "100")
                                .param("ramGb", "0"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        post("/api/products/laptops")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "Laptop")
                                .param("price", "100")
                                .param("storageType", "UNKNOWN"))
                .andExpect(status().isBadRequest());
        verify(products, never()).saveAndFlush(any());
    }

    @Test
    void preservesCharacteristicsWhenOnlyPriceChanges() throws Exception {
        Product product = phone();
        List<ProductSpecification> original = product.getSpecifications();
        when(products.findById(73)).thenReturn(Optional.of(product));
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("price", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(200))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.specifications.ramGb").value("8"))
                .andExpect(jsonPath("$.rearCameras[0].resolutionMp").value(50));
        assertSame(original, product.getSpecifications());
        assertEquals(1, product.getRearCameras().size());
    }

    @Test
    void rejectsEmptyPatchAndDiscountConflict() throws Exception {
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isBadRequest());
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(true);
        when(discounts.lockState()).thenReturn(Optional.of(state));
        mvc.perform(
                        post("/api/products/headphones")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "Audio")
                                .param("price", "100"))
                .andExpect(status().isConflict());
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("price", "100"))
                .andExpect(status().isConflict());
    }

    @Test
    void createsUserFromBodyFormAndHashesPassword() throws Exception {
        mvc.perform(
                        post("/api/users/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "Customer")
                                .param("email", "User@example.com")
                                .param("password", "Password123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(saved.capture());
        assertTrue(
                new BCryptPasswordEncoder()
                        .matches("Password123", saved.getValue().getPasswordHash()));
    }

    @Test
    void updatesUserFromForm() throws Exception {
        User user = User.create(UserRole.CUSTOMER, "user@example.com", "Old name", "hash");
        when(users.findById("user@example.com")).thenReturn(Optional.of(user));
        when(users.changeRole(anyString(), anyString())).thenReturn(1);
        mvc.perform(
                        put("/api/users/user@example.com/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "New name")
                                .param("role", "MANAGER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New name"))
                .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    private Product phone() {
        Product product = new Product();
        product.setArticle(73);
        product.setName("Phone");
        product.setPrice(new BigDecimal("100"));
        product.setBrand(new Brand("DemoBrand"));
        Category category = new Category();
        category.setCode("SMARTPHONE");
        product.setCategory(category);
        product.getSpecifications().add(new ProductSpecification(product, "ramGb", "8"));
        product.getRearCameras().add(new RearCamera(product, "WIDE", new BigDecimal("50"), 1));
        return product;
    }

    @Test
    void rejectsNegativePriceAndOversizedNameInPatch() throws Exception {
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("price", "-5"))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "x".repeat(201)))
                .andExpect(status().isBadRequest());
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "   "))
                .andExpect(status().isBadRequest());
        verify(products, never()).flush();
    }

    @Test
    void emptyNameWithPriceLeavesExistingNameUnchanged() throws Exception {
        Product product = phone();
        when(products.findById(73)).thenReturn(Optional.of(product));
        mvc.perform(
                        patch("/api/products/73/details")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("name", "")
                                .param("price", "150"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.price").value(150));
    }
}
