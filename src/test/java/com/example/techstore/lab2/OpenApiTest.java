package com.example.techstore.lab2;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.config.OpenApiConfig;
import com.example.techstore.config.OpenApiErrorDocumentation;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

class OpenApiTest extends DatabaseFreeTest {
    @Autowired private MockMvc mvc;

    @Test
    void documentsApiPathsExamplesAndSafeResponseSchemas() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("TechStore REST API"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.security[0].basicAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/products'].get").exists())
                .andExpect(jsonPath("$.paths['/api/products'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/users'].post").exists())
                .andExpect(jsonPath("$.paths['/api/discount'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/categories'].get").exists())
                .andExpect(jsonPath("$.paths['/api/specifications/{name}']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/specifications'].get").exists())
                .andExpect(jsonPath("$.paths['/api/specifications'].post").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/categories/{code}/specifications'].get").exists())
                .andExpect(jsonPath("$.paths['/api/categories/{code}/specifications/{name}']").doesNotExist())
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
    void documentsCatalogFieldsAndApplicableErrorResponses() throws Exception {
        String catalog = "$.paths['/api/products'].get";
        String deletion = "$.paths['/api/products/{article}'].delete";
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(catalog + ".parameters[*].description").value(hasSize(8)))
                .andExpect(jsonPath(catalog + ".parameters[*].description")
                        .value(everyItem(not(emptyOrNullString()))))
                .andExpect(jsonPath(catalog + ".responses['200']").exists())
                .andExpect(jsonPath(catalog + ".responses['400']").exists())
                .andExpect(jsonPath(catalog + ".responses['401'].headers['WWW-Authenticate']").exists())
                .andExpect(jsonPath(catalog + ".responses['403']").doesNotExist())
                .andExpect(jsonPath(catalog + ".responses['409']").doesNotExist())
                .andExpect(jsonPath(deletion + ".parameters[0].description").isNotEmpty())
                .andExpect(jsonPath(deletion + ".responses['204']").exists())
                .andExpect(jsonPath(deletion + ".responses['401']").exists())
                .andExpect(jsonPath(deletion + ".responses['403']").exists())
                .andExpect(jsonPath(deletion + ".responses['404'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"))
                .andExpect(jsonPath(deletion + ".responses['409']").exists())
                .andExpect(jsonPath(deletion + ".responses['500']").exists())
                .andExpect(jsonPath(deletion + ".responses['503']").exists())
                .andExpect(jsonPath("$.paths['/api/users'].get.responses['403']").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponse.properties.hint.description").isNotEmpty())
                .andExpect(jsonPath("$.components.schemas.ProductResponse.properties.article.description").isNotEmpty())
                .andExpect(jsonPath("$.components.schemas.PageResponseProductResponse.properties.totalElements.description")
                        .isNotEmpty());
    }

    @ParameterizedTest
    @CsvSource({
        "post, /api/products/smartphones, 400, 404, 409",
        "post, /api/products/tablets, 400, 404, 409",
        "post, /api/products/laptops, 400, 404, 409",
        "post, /api/products/headphones, 400, 404, 409",
        "put, /api/brands/{name}, 400, 404, 409",
        "delete, /api/brands/{name}, 403, 404, 409",
        "post, /api/users, 400, 403, 409",
        "delete, /api/users/{email}, 403, 404, 409",
        "put, /api/users/{email}/form, 400, 404, 409",
        "post, /api/discount, 400, 409, 500",
        "delete, /api/discount, 403, 409, 500",
        "get, /api/categories/{code}/specifications, 401, 404, 503"
    })
    void documentsErrorsForOtherOperations(
            String method, String path, String first, String second, String third) throws Exception {
        String responses = "$.paths['" + path + "']." + method + ".responses";
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(responses + "['" + first + "']").exists())
                .andExpect(jsonPath(responses + "['" + second + "']").exists())
                .andExpect(jsonPath(responses + "['" + third + "'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/ErrorResponse"));
    }

    @Test
    void customizersHandleMissingPathsAndMethods() {
        OpenAPI partialApi = new OpenAPI().paths(new Paths().addPathItem("/api/products", new PathItem()));
        assertDoesNotThrow(() -> new OpenApiConfig().easyOperationOrder().customise(partialApi));
        assertDoesNotThrow(() -> new OpenApiErrorDocumentation().customise(partialApi));
    }

    @Test
    void servesInteractiveSwaggerPage() throws Exception {
        mvc.perform(get("/swagger-ui.html")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/v3/api-docs/swagger-config")).andExpect(status().isOk());
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }
}
