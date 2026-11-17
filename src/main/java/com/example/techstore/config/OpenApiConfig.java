package com.example.techstore.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Paths;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info =
                @Info(
                        title = "TechStore REST API",
                        version = "1.0.0",
                        description =
                                "**Добавить товар:** откройте «1. Товары», выберите «Добавить"
                                    + " смартфон» или другую категорию, нажмите Try it out,"
                                    + " заполните название и цену, затем Execute. JSON вводить не"
                                    + " нужно. Проверьте значения характеристик перед сохранением."
                                    + " **Посмотреть товары:** GET /api/products → Try it out →"
                                    + " Execute. **Изменить цену:** PATCH"
                                    + " /api/products/{article}/details. Расширенные JSON-запросы"
                                    + " находятся в последней группе."))
public class OpenApiConfig {
    @Bean
    public OpenApiCustomizer easyOperationOrder() {
        return api -> {
            Paths original = api.getPaths();
            Paths ordered = new Paths();
            List<String> first =
                    List.of(
                            "/api/products/smartphones",
                            "/api/products/tablets",
                            "/api/products/laptops",
                            "/api/products/headphones",
                            "/api/products",
                            "/api/products/{article}",
                            "/api/products/{article}/details",
                            "/api/users/form",
                            "/api/users",
                            "/api/users/{email}",
                            "/api/users/{email}/form");
            for (String path : first)
                if (original.containsKey(path)) ordered.addPathItem(path, original.get(path));
            original.forEach(
                    (path, item) -> {
                        if (!ordered.containsKey(path)) ordered.addPathItem(path, item);
                    });
            advanced(ordered.get("/api/products").getPost());
            advanced(ordered.get("/api/products/{article}").getPut());
            advanced(ordered.get("/api/users").getPost());
            advanced(ordered.get("/api/users/{email}").getPut());
            api.setPaths(ordered);
        };
    }

    private static void advanced(io.swagger.v3.oas.models.Operation operation) {
        if (operation != null) operation.setTags(List.of("5. Расширенные JSON-запросы"));
    }
}
