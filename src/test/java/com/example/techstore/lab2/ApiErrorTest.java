package com.example.techstore.lab2;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.techstore.exception.PriceCalculationException;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.service.ParallelPriceService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class ApiErrorTest extends DatabaseFreeTest {
    private static final String DISCOUNT = """
            {"percent":10,"threads":2}
            """;

    @Autowired MockMvc mvc;
    @MockitoBean ParallelPriceService calculator;

    @ParameterizedTest
    @CsvSource({
        "size=abc, INVALID_PARAMETER_TYPE",
        "size=101, INVALID_PAGINATION",
        "minPrice=100&maxPrice=10, INVALID_PRICE_RANGE",
        "minPrice=-1, INVALID_PRICE_RANGE"
    })
    void distinguishesInvalidFilters(String query, String code) throws Exception {
        expectError(mvc.perform(get("/api/products?" + query)), 400, code);
    }

    @Test
    void identifiesMissingProduct() throws Exception {
        expectError(mvc.perform(get("/api/products/999")), 404, "PRODUCT_NOT_FOUND")
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    void identifiesDuplicateEmail() throws Exception {
        when(users.existsById("test@example.com")).thenReturn(true);
        expectError(mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email":"test@example.com","name":"Test","password":"Password123","role":"CUSTOMER"}
                        """)), 409, "DUPLICATE_EMAIL");
    }

    @Test
    void identifiesDiscountConflicts() throws Exception {
        prepareDiscount(true);
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content(DISCOUNT)), 409, "DISCOUNT_ALREADY_ACTIVE");
        expectError(mvc.perform(delete("/api/products/42")), 409, "CATALOG_LOCKED_BY_DISCOUNT");
    }

    @Test
    void reportsConflictsWhenThereIsNoDiscountOrNoProduct() throws Exception {
        prepareDiscount(false);
        when(products.findAll()).thenReturn(List.of());
        expectError(mvc.perform(delete("/api/discount")), 409, "NO_ACTIVE_DISCOUNT");
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content(DISCOUNT)), 409, "EMPTY_CATALOG");
    }

    @Test
    void identifiesInvalidCharacteristic() throws Exception {
        prepareDiscount(false);
        String json = ProductApiTest.JSON.replace("\"wireless\":\"true\"", "\"wireless\":\"yes\"");
        expectError(mvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON)
                .content(json)), 400, "INVALID_CHARACTERISTIC_VALUE")
                .andExpect(jsonPath("$.message", containsString("wireless")));
    }

    @Test
    void distinguishesMalformedJsonAndFieldValidation() throws Exception {
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content("{")), 400, "MALFORMED_REQUEST");
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"percent":0,"threads":2}
                        """)), 400, "VALIDATION_FAILED")
                .andExpect(jsonPath("$.details[0]").isNotEmpty());
    }

    @Test
    void preservesAllowHeader() throws Exception {
        expectError(mvc.perform(put("/api/discount")), 405, "METHOD_NOT_ALLOWED")
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("GET")));
    }

    @Test
    void preservesSupportedMediaTypes() throws Exception {
        expectError(mvc.perform(post("/api/products/smartphones")
                .contentType(MediaType.APPLICATION_JSON).content("{}")),
                415, "UNSUPPORTED_MEDIA_TYPE")
                .andExpect(header().string(HttpHeaders.ACCEPT,
                        containsString(MediaType.APPLICATION_FORM_URLENCODED_VALUE)));
    }

    @Test
    void reportsUnavailableDatabaseWithoutInternalDetails() throws Exception {
        when(products.findById(42)).thenThrow(new DataAccessResourceFailureException("internal connection details"));
        expectError(mvc.perform(get("/api/products/42")), 503, "DATABASE_UNAVAILABLE")
                .andExpect(content().string(not(containsString("internal connection details"))));
    }

    @Test
    void reportsIntegrityConflictWithoutInternalDetails() throws Exception {
        prepareDiscount(false);
        Product product = new Product();
        when(products.findById(42)).thenReturn(Optional.of(product));
        doThrow(new DataIntegrityViolationException("internal constraint"))
                .when(products).flush();
        expectError(mvc.perform(delete("/api/products/42")), 409, "DATA_CONFLICT")
                .andExpect(content().string(not(containsString("internal constraint"))));
    }

    @Test
    void reportsMissingDiscountConfigurationFromBothServices() throws Exception {
        expectError(mvc.perform(get("/api/discount")), 500, "DISCOUNT_CONFIGURATION_ERROR");
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content(DISCOUNT)), 500, "DISCOUNT_CONFIGURATION_ERROR");
        expectError(mvc.perform(delete("/api/products/42")), 500, "DISCOUNT_CONFIGURATION_ERROR");
    }

    @Test
    void reportsCalculationFailureWithoutLeakingCauseOrSavingChanges() throws Exception {
        prepareDiscount(false);
        Product product = new Product();
        product.setArticle(42);
        product.setName("Phone");
        product.setPrice(BigDecimal.TEN);
        when(products.findAll()).thenReturn(List.of(product));
        when(calculator.calculate(any(), any(), anyInt())).thenThrow(new PriceCalculationException(
                "internal worker message", new ArithmeticException("internal cause")));
        expectError(mvc.perform(post("/api/discount").contentType(MediaType.APPLICATION_JSON)
                .content(DISCOUNT)), 500, "PRICE_CALCULATION_FAILED")
                .andExpect(content().string(not(containsString("internal"))));
        verify(backups, never()).save(any());
        verify(products, never()).flush();
    }

    private void prepareDiscount(boolean active) {
        DiscountState state = new DiscountState();
        state.setId(1);
        state.setActive(active);
        when(discounts.lockState()).thenReturn(Optional.of(state));
    }

    private ResultActions expectError(ResultActions result, int statusCode, String code)
            throws Exception {
        return result.andExpect(status().is(statusCode))
                .andExpect(jsonPath("$.status").value(statusCode))
                .andExpect(jsonPath("$.error").value(code))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.hint").isNotEmpty())
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }
}
