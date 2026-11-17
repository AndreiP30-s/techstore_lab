package com.example.techstore.lab2;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class OpenApiTest extends DatabaseFreeTest {
    @Autowired private MockMvc mvc;

    @Test
    void documentsApiPathsExamplesAndSafeResponseSchemas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TechStore REST API"))
                .andExpect(jsonPath("$.paths['/api/products'].get").exists())
                .andExpect(jsonPath("$.paths['/api/products'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/users'].post").exists())
                .andExpect(jsonPath("$.paths['/api/discount'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/categories'].get").exists())
                .andExpect(jsonPath("$.components.schemas.ProductRequest.example").exists())
                .andExpect(jsonPath("$.components.schemas.UserResponse.properties.email").exists())
                .andExpect(
                        jsonPath("$.components.schemas.UserResponse.properties.password")
                                .doesNotExist())
                .andExpect(
                        jsonPath("$.components.schemas.UserResponse.properties.passwordHash")
                                .doesNotExist());
    }

    @Test
    void exposesBodyFormsAndKeepsJsonInAdvancedGroup() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                        "$.paths['/api/products/smartphones'].post.requestBody.content['application/x-www-form-urlencoded']")
                                .exists())
                .andExpect(
                        jsonPath(
                                        "$.paths['/api/users/form'].post.requestBody.content['application/x-www-form-urlencoded']")
                                .exists())
                .andExpect(jsonPath("$.paths['/api/users/form'].post.parameters").doesNotExist())
                .andExpect(
                        jsonPath("$.paths['/api/products'].post.tags[0]")
                                .value("5. Расширенные JSON-запросы"))
                .andExpect(
                        jsonPath("$.paths['/api/products/smartphones'].post.tags[0]")
                                .value("1. Товары"));
    }

    @Test
    void servesInteractiveSwaggerPage() throws Exception {
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
