package com.example.techstore.service;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** Применение и отмена скидки с сохранением состояния в БД. */
public interface DiscountService {
    record Status(
            @Schema(description = "Скидка сейчас действует") boolean active) {}

    record Result(
            @Schema(description = "Количество товаров с изменённой ценой", example = "12")
                    int affected) {}

    Status status();

    Result apply(BigDecimal percent, int threads);

    Result cancel();
}
