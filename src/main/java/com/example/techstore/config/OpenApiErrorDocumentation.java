package com.example.techstore.config;

import static io.swagger.v3.oas.models.PathItem.HttpMethod.DELETE;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.GET;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.PATCH;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.POST;
import static io.swagger.v3.oas.models.PathItem.HttpMethod.PUT;

import com.example.techstore.exception.ErrorResponse;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OpenApiErrorDocumentation implements OpenApiCustomizer {
    private static final String ERROR_SCHEMA = "#/components/schemas/ErrorResponse";
    private static final String CATALOG_CONFLICT =
            "Каталог заблокирован действующей скидкой или нарушено ограничение целостности данных";
    private static final String DISCOUNT_CONFIG_ERROR = "Сервис скидок не настроен";
    private static final String MEDIA_TYPE_ERROR = "Тип содержимого запроса не поддерживается";
    private static final String ARTICLE_ERROR = "Артикул должен быть целым числом";
    private static final String PRODUCT_NOT_FOUND = "Товар с таким артикулом не найден";
    private static final String INVALID_EMAIL = "Некорректный формат email";
    private static final String USER_NOT_FOUND = "Пользователь с таким email не найден";
    private static final String BRAND_NOT_FOUND = "Бренд с таким названием не найден";

    @Override
    public void customise(OpenAPI api) {
        Paths paths = api.getPaths();
        if (paths == null) return;

        if (api.getComponents() == null) api.setComponents(new Components());
        ModelConverters.getInstance(api.getOpenapi().startsWith("3.1")).readAll(ErrorResponse.class)
                .forEach(api.getComponents()::addSchemas);

        paths.forEach((path, item) -> {
            if (!path.startsWith("/api/")) return;
            item.readOperationsMap().forEach((method, operation) -> {
                ApiResponse unauthorized =
                        response("Укажите действительные email и пароль через HTTP Basic")
                                .addHeaderObject("WWW-Authenticate", new Header()
                                        .schema(new StringSchema().example("Basic realm=\"TechStore\"")));
                operation.getResponses().putIfAbsent("401", unauthorized);
                if (path.equals("/api/users") || path.startsWith("/api/users/") || method != GET) {
                    operation.getResponses().putIfAbsent("403",
                            response("У пользователя нет прав на эту операцию"));
                }
                operation.getResponses().putIfAbsent("503", response("База данных недоступна"));
            });
        });

        documentProducts(paths);
        documentUsers(paths);
        documentReferences(paths);
        documentDiscount(paths);
    }

    private static void documentProducts(Paths paths) {
        String products = "/api/products";
        String product = products + "/{article}";
        errors(paths, products, GET, Map.of(
                "400", "Неверный фильтр, диапазон цен или параметры пагинации"));
        errors(paths, product, GET, Map.of("400", ARTICLE_ERROR, "404", PRODUCT_NOT_FOUND));
        errors(paths, product, DELETE, Map.of(
                "400", ARTICLE_ERROR,
                "404", PRODUCT_NOT_FOUND,
                "409", CATALOG_CONFLICT,
                "500", DISCOUNT_CONFIG_ERROR));
        productWrite(paths, products, POST, "Некорректный JSON или данные товара",
                "Бренд, категория или характеристика не найдены");
        productWrite(paths, product, PUT, "Неверный артикул, JSON или данные товара",
                "Товар, бренд, категория или характеристика не найдены");
        productWrite(paths, product + "/details", PATCH, "Неверный артикул или данные формы",
                "Товар или указанный бренд не найдены");
        for (String category : List.of("smartphones", "tablets", "laptops", "headphones")) {
            productWrite(paths, products + "/" + category, POST, "Некорректные данные формы товара",
                    "Категория или характеристика не найдена");
        }
    }

    private static void productWrite(
            Paths paths, String path, PathItem.HttpMethod method, String badRequest, String notFound) {
        errors(paths, path, method, Map.of(
                "400", badRequest,
                "404", notFound,
                "409", CATALOG_CONFLICT,
                "415", MEDIA_TYPE_ERROR,
                "500", DISCOUNT_CONFIG_ERROR));
    }

    private static void documentUsers(Paths paths) {
        String users = "/api/users";
        String user = users + "/{email}";
        errors(paths, users, GET, Map.of("400", "Некорректное значение фильтра role"));
        errors(paths, user, GET, Map.of("400", INVALID_EMAIL, "404", USER_NOT_FOUND));
        errors(paths, user, DELETE, Map.of(
                "400", INVALID_EMAIL, "404", USER_NOT_FOUND,
                "409", "У пользователя есть связанные заказы"));
        for (String path : List.of(users, users + "/form")) {
            errors(paths, path, POST, Map.of(
                    "400", "Некорректные данные для создания пользователя",
                    "409", "Указанный email уже занят", "415", MEDIA_TYPE_ERROR));
        }
        for (String path : List.of(user, user + "/form")) {
            errors(paths, path, PUT, Map.of(
                    "400", "Некорректный email или данные для обновления",
                    "404", USER_NOT_FOUND, "409", "Нарушено ограничение целостности данных",
                    "415", MEDIA_TYPE_ERROR));
        }
    }

    private static void documentReferences(Paths paths) {
        String brands = "/api/brands";
        String brand = brands + "/{name}";
        errors(paths, brands, POST, Map.of(
                "400", "Некорректное название бренда",
                "409", "Бренд с таким названием уже существует",
                "415", MEDIA_TYPE_ERROR));
        errors(paths, brand, GET, Map.of("404", BRAND_NOT_FOUND));
        errors(paths, brand, PUT, Map.of(
                "400", "Некорректное новое название бренда",
                "404", BRAND_NOT_FOUND,
                "409", "Бренд используется товарами или новое название уже занято",
                "415", MEDIA_TYPE_ERROR));
        errors(paths, brand, DELETE, Map.of(
                "404", BRAND_NOT_FOUND, "409", "Бренд используется товарами"));
        errors(paths, "/api/categories/{code}/specifications", GET, Map.of(
                "404", "Категория с таким кодом не найдена"));
    }

    private static void documentDiscount(Paths paths) {
        String discount = "/api/discount";
        errors(paths, discount, GET, Map.of("500", DISCOUNT_CONFIG_ERROR));
        errors(paths, discount, POST, Map.of(
                "400", "Некорректные параметры скидки или итоговая цена не больше нуля",
                "409", "Скидка уже действует, каталог пуст или нарушено ограничение целостности данных",
                "415", MEDIA_TYPE_ERROR,
                "500", "Сервис скидок не настроен или произошёл сбой расчёта цен"));
        errors(paths, discount, DELETE, Map.of(
                "409", "Нет действующей скидки или восстановить цены мешает конфликт данных",
                "500", DISCOUNT_CONFIG_ERROR));
    }

    private static void errors(
            Paths paths, String path, PathItem.HttpMethod method, Map<String, String> descriptions) {
        PathItem item = paths.get(path);
        if (item == null) return;
        Operation operation = item.readOperationsMap().get(method);
        if (operation == null) return;
        descriptions.forEach((status, description) ->
                operation.getResponses().putIfAbsent(status, response(description)));
    }

    private static ApiResponse response(String description) {
        return new ApiResponse().description(description).content(new Content()
                .addMediaType("application/json", new MediaType().schema(new Schema<>().$ref(ERROR_SCHEMA))));
    }
}
