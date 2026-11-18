package com.example.techstore.service;
import java.math.BigDecimal;

public record PriceQuote(long article, BigDecimal originalPrice, BigDecimal discountedPrice, String threadName) { }
