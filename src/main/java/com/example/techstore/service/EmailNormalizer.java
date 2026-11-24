package com.example.techstore.service;

import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.ValidationException;
import java.util.Locale;

public final class EmailNormalizer {
    private EmailNormalizer() {}

    public static String normalize(String email) {
        if (email == null) throw new ValidationException(ErrorCode.INVALID_EMAIL, "Email обязателен");
        String value = email.strip().toLowerCase(Locale.ROOT);
        if (value.length() > 254 || !value.matches("[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+"))
            throw new ValidationException(ErrorCode.INVALID_EMAIL, "Некорректный email");
        return value;
    }
}
