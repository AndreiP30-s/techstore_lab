package com.example.techstore.service;

import com.example.techstore.dto.ProductRequest;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.InvalidCharacteristicException;
import com.example.techstore.exception.ValidationException;
import com.example.techstore.model.product.HeadphoneType;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.StorageType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Проверки обязательных характеристик по категориям. */
@Component
public class ProductRules {
    public static Set<String> required(ProductCategory category) {
        if (category == ProductCategory.HEADPHONES)
            return Set.of(
                    "headphoneType",
                    "wireless",
                    "noiseCancelling",
                    "minFrequencyHz",
                    "maxFrequencyHz");
        Set<String> result =
                new HashSet<>(
                        Set.of(
                                "processor",
                                "ramGb",
                                "storageGb",
                                "screenDiagonalInches",
                                "frontCameraMp"));
        switch (category) {
            case SMARTPHONE -> result.addAll(Set.of("batteryCapacityMah", "supports5g"));
            case TABLET -> result.addAll(Set.of("batteryCapacityMah", "cellular"));
            case LAPTOP -> result.add("storageType");
            default -> throw new ValidationException(
                    ErrorCode.UNKNOWN_CATEGORY,
                    "Неизвестная категория: " + category);
        }
        return Set.copyOf(result);
    }

    public void validate(ProductRequest request) {
        Map<String, String> values = request.specifications();
        for (String name : required(request.category())) {
            if (!values.containsKey(name) || values.get(name) == null || values.get(name).isBlank())
                throw new InvalidCharacteristicException(
                        ErrorCode.MISSING_CHARACTERISTIC,
                        "Не указана характеристика: " + name);
        }
        if (request.category() == ProductCategory.HEADPHONES) {
            enumValue(HeadphoneType.class, values.get("headphoneType"), "headphoneType");
            bool(values, "wireless");
            bool(values, "noiseCancelling");
            int min = positiveInteger(values, "minFrequencyHz");
            int max = positiveInteger(values, "maxFrequencyHz");
            if (min >= max)
                throw new InvalidCharacteristicException(
                        ErrorCode.INVALID_CHARACTERISTIC_VALUE,
                        "Минимальная частота должна быть меньше максимальной",
                        "Например, укажите minFrequencyHz=20 и maxFrequencyHz=20000.");
        } else {
            positiveInteger(values, "ramGb");
            positiveInteger(values, "storageGb");
            positiveDecimal(values, "screenDiagonalInches");
            positiveDecimal(values, "frontCameraMp");
            switch (request.category()) {
                case SMARTPHONE -> {
                    positiveInteger(values, "batteryCapacityMah");
                    bool(values, "supports5g");
                }
                case TABLET -> {
                    positiveInteger(values, "batteryCapacityMah");
                    bool(values, "cellular");
                }
                case LAPTOP ->
                        enumValue(StorageType.class, values.get("storageType"), "storageType");
                default -> throw new ValidationException(
                        ErrorCode.UNKNOWN_CATEGORY,
                        "Неизвестная категория: " + request.category());
            }
        }
        validateCameras(request);
    }

    private void validateCameras(ProductRequest request) {
        boolean needsCameras =
                request.category() == ProductCategory.SMARTPHONE
                        || request.category() == ProductCategory.TABLET;
        if (needsCameras && request.rearCameras().isEmpty())
            throw new ValidationException(
                    ErrorCode.INVALID_CAMERA_CONFIGURATION,
                    "Нужна хотя бы одна задняя камера",
                    "Добавьте хотя бы одну камеру в rearCameras.");
        if (!needsCameras && !request.rearCameras().isEmpty())
            throw new ValidationException(
                    ErrorCode.INVALID_CAMERA_CONFIGURATION,
                    "У этой категории нет задних камер",
                    "Оставьте список rearCameras пустым.");
        Set<String> groups = new HashSet<>();
        int total = 0;
        for (ProductRequest.RearCameraRequest camera : request.rearCameras()) {
            String key =
                    camera.type().name()
                            + ":"
                            + camera.resolutionMp().stripTrailingZeros().toPlainString();
            if (!groups.add(key))
                throw new ValidationException(
                        ErrorCode.INVALID_CAMERA_CONFIGURATION,
                        "В списке есть одинаковые задние камеры",
                        "Объедините камеры с одинаковым типом и разрешением через quantity.");
            total += camera.quantity();
            if (total > 32)
                throw new ValidationException(
                        ErrorCode.INVALID_CAMERA_CONFIGURATION,
                        "Не больше 32 задних камер",
                        "Уменьшите суммарное значение quantity до 32 или меньше.");
        }
    }

    private int positiveInteger(Map<String, String> values, String name) {
        String raw = values.get(name).strip();
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw invalidValue(name, raw, "требуется положительное целое число", "Укажите целое число больше нуля, например 8.");
        }
        if (value <= 0)
            throw invalidValue(name, raw, "число должно быть положительным", "Укажите целое число больше нуля, например 8.");
        return value;
    }

    private void positiveDecimal(Map<String, String> values, String name) {
        String raw = values.get(name).strip();
        BigDecimal value;
        try {
            value = new BigDecimal(raw);
        } catch (NumberFormatException e) {
            throw invalidValue(name, raw, "требуется положительное число", "Укажите положительное число, например 6.5.");
        }
        if (value.signum() <= 0)
            throw invalidValue(name, raw, "число должно быть положительным", "Укажите положительное число, например 6.5.");
    }

    private void bool(Map<String, String> values, String name) {
        String value = values.get(name).strip();
        if (!value.equals("true") && !value.equals("false"))
            throw invalidValue(name, value, "требуется логическое значение", "Укажите true или false.");
    }

    private <E extends Enum<E>> void enumValue(Class<E> type, String value, String name) {
        String normalized = value.strip();
        E[] options = type.getEnumConstants();
        if (Arrays.stream(options).noneMatch(option -> option.name().equals(normalized)))
            throw invalidValue(name, normalized, "неизвестное значение", "Допустимые значения: " + Arrays.toString(options));
    }

    private InvalidCharacteristicException invalidValue(
            String name, String value, String reason, String hint) {
        return new InvalidCharacteristicException(
                ErrorCode.INVALID_CHARACTERISTIC_VALUE,
                name + ": значение '" + value + "', " + reason,
                hint);
    }
}
