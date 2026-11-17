package com.example.techstore.service;

import java.math.BigDecimal;

/** Применение и отмена скидки с сохранением состояния в БД. */
public interface DiscountService {
    record Status(boolean active) {}

    record Result(int affected) {}

    Status status();

    Result apply(BigDecimal percent, int threads);

    Result cancel();
}
