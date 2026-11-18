package com.example.techstore.controller;

import com.example.techstore.dto.PageResponse;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.dto.ProductResponse;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
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
            @io.swagger.v3.oas.annotations.Parameter(
                            description = "Часть названия; можно не заполнять")
                    @RequestParam(required = false)
                    String name,
            @io.swagger.v3.oas.annotations.Parameter(
                            description = "Точное название бренда; можно не заполнять")
                    @RequestParam(required = false)
                    String brand,
            @RequestParam(required = false) ProductCategory category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @io.swagger.v3.oas.annotations.Parameter(
                            description = "Номер страницы с нуля; можно оставить 0")
                    @RequestParam(defaultValue = "0")
                    int page,
            @io.swagger.v3.oas.annotations.Parameter(
                            description = "Количество товаров на странице; можно оставить 20")
                    @RequestParam(defaultValue = "20")
                    int size) {
        return service.list(name, brand, category, minPrice, maxPrice, available, page, size);
    }

    @GetMapping("/{article}")
    @Operation(summary = "Найти товар по артикулу")
    public ProductResponse get(@PathVariable int article) {
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
            @PathVariable int article, @Valid @RequestBody ProductRequest request) {
        return service.update(article, request);
    }

    @DeleteMapping("/{article}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить товар")
    public void delete(@PathVariable int article) {
        service.delete(article);
    }
}
