package com.example.techstore.controller;

import com.example.techstore.dto.PageResponse;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.dto.ProductResponse;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.net.URI;

@Tag(name = "1. Товары")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Список товаров и фильтры")
    public PageResponse<ProductResponse> list(
            @Parameter(description = "Часть названия товара", example = "Смартфон")
                    @RequestParam(required = false) String name,
            @Parameter(description = "Точное название бренда", example = "DemoBrand")
                    @RequestParam(required = false) String brand,
            @Parameter(description = "Категория товара", example = "SMARTPHONE")
                    @RequestParam(required = false) ProductCategory category,
            @Parameter(description = "Нижняя граница цены в рублях включительно", example = "10000")
                    @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Верхняя граница цены в рублях включительно", example = "50000")
                    @RequestParam(required = false) BigDecimal maxPrice,
            @Parameter(description = "Доступность для заказа. Без параметра возвращаются все товары")
                    @RequestParam(required = false) Boolean available,
            @Parameter(description = "Номер страницы, начиная с нуля", example = "0")
                    @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Размер страницы от 1 до 100", example = "20")
                    @RequestParam(defaultValue = "20") int size) {
        return service.list(name, brand, category, minPrice, maxPrice, available, page, size);
    }

    @GetMapping("/{article}")
    @Operation(summary = "Найти товар по артикулу")
    public ProductResponse get(
            @Parameter(description = "Артикул товара из каталога", example = "100001")
                    @PathVariable int article) {
        return service.get(article);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать товар — расширенный JSON")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = service.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.article()))
                .body(response);
    }

    @PutMapping("/{article}")
    @Operation(summary = "Заменить все поля и характеристики — JSON")
    public ProductResponse update(
            @Parameter(description = "Артикул товара из каталога", example = "100001")
                    @PathVariable int article,
            @Valid @RequestBody ProductRequest request) {
        return service.update(article, request);
    }

    @DeleteMapping("/{article}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Удалить товар",
            description = "Доступно администратору. При активной скидке удаление заблокировано.")
    public void delete(
            @Parameter(description = "Артикул товара из каталога", example = "100001")
                    @PathVariable int article) {
        service.delete(article);
    }
}
