package com.example.techstore.service;

import java.util.Locale;

public final class EmailNormalizer {
    private EmailNormalizer() {}

    public static String normalize(String email) {
        if (email == null) throw new IllegalArgumentException("Email обязателен");
        String value = email.strip().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !value.matches("[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+"))
            throw new IllegalArgumentException("Некорректный email");
        return value;
    }
}
