package com.example.techstore.lab2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.techstore.model.catalog.Brand;
import com.example.techstore.model.catalog.Category;
import com.example.techstore.model.discount.DiscountState;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

@ExtendWith(OutputCaptureExtension.class)
class SecurityApiTest extends DatabaseFreeTest {
    private static final String EMAIL = "buyer@example.com";
    private static final String PASSWORD = "BuyerLocalTest18";

    @Autowired private MockMvc mvc;
    @Autowired private PasswordEncoder encoder;

    @Test
    void authenticatesWithExistingBcryptHashAndDoesNotKeepSession() throws Exception {
        User buyer = User.create(UserRole.CUSTOMER, EMAIL, "Покупатель", encoder.encode(PASSWORD));
        when(users.findById(EMAIL)).thenReturn(Optional.of(buyer));
        when(products.findAll(any(Specification.class), any(Pageable.class))).thenReturn(Page.empty());

        mvc.perform(get("/api/products").with(httpBasic("Buyer@example.com", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("JSESSIONID"));

        mvc.perform(get("/api/products").with(httpBasic(EMAIL, "WrongPassword18")))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")))
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.hint").isNotEmpty());

        mvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, containsString("Basic")))
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
    }

    @ParameterizedTest
    @CsvSource({
        "GET, /api/users, MANAGER",
        "POST, /api/users/form, MANAGER",
        "PUT, /api/users/buyer@example.com/form, MANAGER",
        "POST, /api/products/smartphones, CUSTOMER",
        "PUT, /api/products/42, CUSTOMER",
        "PATCH, /api/products/42/details, CUSTOMER",
        "POST, /api/brands, CUSTOMER",
        "PUT, /api/brands/Demo, CUSTOMER",
        "PATCH, /api/brands/Demo, CUSTOMER",
        "POST, /api/discount, CUSTOMER",
        "DELETE, /api/brands/Demo, MANAGER",
        "DELETE, /api/discount, MANAGER",
        "PUT, /api/discount, ADMIN",
        "GET, /error, ADMIN"
    })
    void deniesRoutesBeforeTheyReachBusinessCode(String method, String path, String role)
            throws Exception {
        mvc.perform(request(HttpMethod.valueOf(method), path).with(user("test").roles(role)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.hint").isNotEmpty());
        verifyNoInteractions(products, brands, users, discounts);
    }

    @Test
    void managerCanPatchProductAndSuccessIsLogged(CapturedOutput output) throws Exception {
        Product product = prepareProduct();
        mvc.perform(patch("/api/products/42/details").with(user("manager").roles("MANAGER"))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("price", "4500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(4500));
        assertThat(product.getPrice()).isEqualByComparingTo("4500");
        assertThat(output).contains("Операция выполнена: пользователь=manager, метод=PATCH, путь=/api/products/42/details");
    }

    @Test
    void onlyAdminCanDeleteAndRefusalIsLogged(CapturedOutput output) throws Exception {
        mvc.perform(delete("/api/products/42"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("AUTHENTICATION_REQUIRED"));
        mvc.perform(delete("/api/products/42").with(user("manager").roles("MANAGER")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("ACCESS_DENIED"));
        verifyNoInteractions(products, discounts);

        Product product = prepareProduct();
        mvc.perform(delete("/api/products/42").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());
        verify(products).delete(product);
        assertThat(output).contains("Доступ запрещён: пользователь=manager, метод=DELETE, путь=/api/products/42")
                .contains("Операция выполнена: пользователь=admin, метод=DELETE, путь=/api/products/42");
    }

    private Product prepareProduct() {
        DiscountState state = new DiscountState();
        when(discounts.lockState()).thenReturn(Optional.of(state));
        Product product = new Product();
        product.setArticle(42);
        product.setName("Наушники");
        product.setBrand(new Brand("Demo"));
        product.setPrice(BigDecimal.TEN);
        Category category = new Category();
        category.setCode("HEADPHONES");
        product.setCategory(category);
        when(products.findById(42)).thenReturn(Optional.of(product));
        return product;
    }
}
