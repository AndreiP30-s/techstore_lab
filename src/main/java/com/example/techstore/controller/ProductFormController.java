package com.example.techstore.controller;

import com.example.techstore.dto.*;
import com.example.techstore.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "1. Товары")
@RestController
@RequestMapping("/api/products")
public class ProductFormController {
    private final ProductService service;

    public ProductFormController(ProductService service) {
        this.service = service;
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = ProductForms.Smartphone.class)))
    @PostMapping(value = "/smartphones", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Добавить смартфон — заполнить поля",
            description =
                    "Обязательны название и цена. Остальные поля уже имеют значения по умолчанию:"
                            + " проверьте характеристики перед сохранением. Новый бренд создастся"
                            + " автоматически.")
    public ResponseEntity<ProductResponse> smartphone(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Valid @ModelAttribute
                    ProductForms.Smartphone form) {
        return created(form.toRequest());
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = ProductForms.Tablet.class)))
    @PostMapping(value = "/tablets", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить планшет — заполнить поля")
    public ResponseEntity<ProductResponse> tablet(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Valid @ModelAttribute
                    ProductForms.Tablet form) {
        return created(form.toRequest());
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = ProductForms.Laptop.class)))
    @PostMapping(value = "/laptops", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить ноутбук — заполнить поля")
    public ResponseEntity<ProductResponse> laptop(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Valid @ModelAttribute
                    ProductForms.Laptop form) {
        return created(form.toRequest());
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = ProductForms.Headphones.class)))
    @PostMapping(value = "/headphones", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить наушники — заполнить поля")
    public ResponseEntity<ProductResponse> headphones(
            @io.swagger.v3.oas.annotations.Parameter(hidden = true) @Valid @ModelAttribute
                    ProductForms.Headphones form) {
        return created(form.toRequest());
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = ProductForms.Details.class)))
    @PatchMapping(
            value = "/{article}/details",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(
            summary = "Изменить цену, название или доступность",
            description =
                    "Введите артикул и только те поля, которые хотите изменить. Пустые поля"
                            + " оставляют прежние значения. Характеристики и камеры сохраняются.")
    public ProductResponse details(
            @Parameter(description = "Артикул товара из каталога", example = "100001")
                    @PathVariable int article,
            @Parameter(hidden = true) @Valid @ModelAttribute
                    ProductForms.Details form) {
        return service.updateDetails(article, form);
    }

    private ResponseEntity<ProductResponse> created(ProductRequest request) {
        ProductResponse product = service.createFromForm(request);
        return ResponseEntity.created(URI.create("/api/products/" + product.article()))
                .body(product);
    }
}
