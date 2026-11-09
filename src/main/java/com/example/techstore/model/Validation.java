package com.example.techstore.model;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Locale;
public final class Validation {
    private Validation() { }
    public static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) { throw new IllegalArgumentException(field + " must not be blank"); }
        return value.strip();
    }
    public static long article(long value) {
        if (value <= 0) { throw new IllegalArgumentException("Артикул должен быть целым положительным числом."); }
        return value;
    }

    public static String email(String value) {
        if (value == null) { throw new IllegalArgumentException("Введите email, например name@example.org."); }
        String normalized = value.strip().toLowerCase(Locale.ROOT);
        if (normalized.length() > 254 || !normalized.matches("[^\\s@]+@[^\\s@.]+(\\.[^\\s@.]+)+")) {
            throw new IllegalArgumentException("Некорректный email. Пример: name@example.org.");
        }
        return normalized;
    }

    public static int positive(int value, String field) {
        if (value <= 0) { throw new IllegalArgumentException(field + " must be positive"); }
        return value;
    }
    public static double positive(double value, String field) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(field + " must be finite and positive");
        }
        return value;
    }
    public static BigDecimal price(BigDecimal value) {
        Objects.requireNonNull(value, "price");
        if (value.signum() < 0 || value.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Price must be nonnegative with at most two decimal places");
        }
        return value.setScale(2);
    }
}
