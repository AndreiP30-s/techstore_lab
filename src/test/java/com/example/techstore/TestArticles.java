package com.example.techstore;

import java.util.concurrent.atomic.AtomicInteger;

/** Уникальные артикулы для независимых тестовых объектов. */
public final class TestArticles {
    private static final AtomicInteger SEQUENCE = new AtomicInteger();
    private TestArticles() { }
    public static long next() { return SEQUENCE.incrementAndGet(); }
}
