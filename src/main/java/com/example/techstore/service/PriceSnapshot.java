package com.example.techstore.service;

import java.math.BigDecimal;

/** Неизменяемые данные для расчёта цены в рабочих потоках. */
public record PriceSnapshot(Integer article, String name, BigDecimal price) {}
