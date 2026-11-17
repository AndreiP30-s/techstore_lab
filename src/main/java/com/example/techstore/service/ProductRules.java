package com.example.techstore.service;

import com.example.techstore.dto.ProductRequest;
import com.example.techstore.model.product.HeadphoneType;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.StorageType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
            default -> throw new IllegalArgumentException("Неизвестная категория");
        }
        return Set.copyOf(result);
    }

    public void validate(ProductRequest request) {
        Map<String, String> values = request.specifications();
        for (String name : required(request.category())) {
            if (!values.containsKey(name) || values.get(name) == null || values.get(name).isBlank())
                throw new IllegalArgumentException("Не указана характеристика: " + name);
        }
        if (request.category() == ProductCategory.HEADPHONES) {
            enumValue(HeadphoneType.class, values.get("headphoneType"), "headphoneType");
            bool(values, "wireless");
            bool(values, "noiseCancelling");
            int min = positiveInteger(values, "minFrequencyHz");
            int max = positiveInteger(values, "maxFrequencyHz");
            if (min >= max)
                throw new IllegalArgumentException(
                        "Минимальная частота должна быть меньше максимальной");
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
                default -> throw new IllegalArgumentException("Неизвестная категория");
            }
        }
        validateCameras(request);
    }

    private void validateCameras(ProductRequest request) {
        boolean needsCameras =
                request.category() == ProductCategory.SMARTPHONE
                        || request.category() == ProductCategory.TABLET;
        if (needsCameras && request.rearCameras().isEmpty())
            throw new IllegalArgumentException("Нужна хотя бы одна задняя камера");
        if (!needsCameras && !request.rearCameras().isEmpty())
            throw new IllegalArgumentException("У этой категории нет задних камер");
        Set<String> groups = new HashSet<>();
        int total = 0;
        for (ProductRequest.RearCameraRequest camera : request.rearCameras()) {
            String key =
                    camera.type().name()
                            + ":"
                            + camera.resolutionMp().stripTrailingZeros().toPlainString();
            if (!groups.add(key))
                throw new IllegalArgumentException("Одинаковые камеры объедините через quantity");
            total += camera.quantity();
            if (total > 32) throw new IllegalArgumentException("Не больше 32 задних камер");
        }
    }

    private int positiveInteger(Map<String, String> values, String name) {
        try {
            int value = Integer.parseInt(values.get(name).strip());
            if (value <= 0) throw new IllegalArgumentException();
            return value;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(name + ": требуется положительное целое число");
        }
    }

    private void positiveDecimal(Map<String, String> values, String name) {
        try {
            if (new BigDecimal(values.get(name).strip()).signum() <= 0)
                throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(name + ": требуется положительное число");
        }
    }

    private void bool(Map<String, String> values, String name) {
        String value = values.get(name).strip();
        if (!value.equals("true") && !value.equals("false"))
            throw new IllegalArgumentException(name + ": требуется true или false");
    }

    private <E extends Enum<E>> void enumValue(Class<E> type, String value, String name) {
        try {
            Enum.valueOf(type, value.strip());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестное значение " + name);
        }
    }
}
