package com.example.techstore.controller;

import com.example.techstore.service.DiscountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "4. Скидки")
@RestController
@RequestMapping("/api/discount")
public class DiscountController {
    private final DiscountService service;

    public DiscountController(DiscountService service) {
        this.service = service;
    }

    @Schema(
            name = "DiscountRequest",
            example =
                    """
                    {"percent":10,"threads":3}
                    """)
    public record Request(
            @Schema(description = "Размер скидки в процентах", example = "10")
                    @NotNull @DecimalMin("0.01") @DecimalMax("99.99") BigDecimal percent,
            @Schema(description = "Число потоков расчёта от 1 до 32", example = "3")
                    @Min(1) @Max(32) int threads) {}

    @GetMapping
    @Operation(summary = "Состояние скидки")
    public DiscountService.Status status() {
        return service.status();
    }

    @PostMapping
    @Operation(summary = "Применить скидку ко всему каталогу")
    public DiscountService.Result apply(@Valid @RequestBody Request request) {
        return service.apply(request.percent(), request.threads());
    }

    @DeleteMapping
    @Operation(summary = "Отменить скидку и восстановить цены")
    public DiscountService.Result cancel() {
        return service.cancel();
    }
}
