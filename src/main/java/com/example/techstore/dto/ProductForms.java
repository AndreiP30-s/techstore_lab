package com.example.techstore.dto;

import com.example.techstore.model.product.*;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Поля форм добавления и изменения товаров. Преобразуются в ProductRequest
 * с общими проверками для форм и JSON-запросов.
 */
public final class ProductForms {
    private ProductForms() {}

    @Schema(name = "SmartphoneForm")
    public record Smartphone(
            @Schema(description = "Название товара", example = "Новый товар")
                    @NotBlank
                    @Size(max = 200)
                    String name,
            @Schema(
                            description = "Бренд. Новый бренд будет создан автоматически",
                            defaultValue = "DemoBrand")
                    @Size(max = 100)
                    String brand,
            @Schema(description = "Цена в рублях", example = "30000")
                    @NotNull
                    @DecimalMin("0.01")
                    @Digits(integer = 7, fraction = 2)
                    BigDecimal price,
            @Schema(description = "Процессор", defaultValue = "Demo CPU") @Size(max = 1000)
                    String processor,
            @Schema(description = "Оперативная память, ГБ", defaultValue = "8") @Min(1)
                    Integer ramGb,
            @Schema(description = "Память устройства, ГБ", defaultValue = "256") @Min(1)
                    Integer storageGb,
            @Schema(description = "Диагональ экрана, дюймы", defaultValue = "6.5")
                    @DecimalMin("0.001")
                    BigDecimal screenInches,
            @Schema(description = "Фронтальная камера, Мп", defaultValue = "12")
                    @DecimalMin("0.001")
                    BigDecimal frontCameraMp,
            @Schema(description = "Основная задняя камера, Мп", defaultValue = "50")
                    @DecimalMin("0.001")
                    @Digits(integer = 5, fraction = 3)
                    BigDecimal rearCameraMp,
            @Schema(description = "Ёмкость батареи, мА·ч", defaultValue = "5000") @Min(1)
                    Integer batteryMah,
            @Schema(description = "Поддержка 5G", defaultValue = "false") Boolean supports5g) {
        public ProductRequest toRequest() {
            Map<String, String> values =
                    computingValues(processor, ramGb, storageGb, screenInches, frontCameraMp);
            values.put("batteryCapacityMah", Integer.toString(or(batteryMah, 5000)));
            values.put("supports5g", Boolean.toString(Boolean.TRUE.equals(supports5g)));
            return request(
                    name,
                    brand,
                    price,
                    ProductCategory.SMARTPHONE,
                    values,
                    camera(rearCameraMp, 50));
        }
    }

    @Schema(name = "TabletForm")
    public record Tablet(
            @Schema(description = "Название товара", example = "Новый товар")
                    @NotBlank
                    @Size(max = 200)
                    String name,
            @Schema(
                            description = "Бренд. Новый бренд будет создан автоматически",
                            defaultValue = "DemoBrand")
                    @Size(max = 100)
                    String brand,
            @Schema(description = "Цена в рублях", example = "30000")
                    @NotNull
                    @DecimalMin("0.01")
                    @Digits(integer = 7, fraction = 2)
                    BigDecimal price,
            @Schema(description = "Процессор", defaultValue = "Demo CPU") @Size(max = 1000)
                    String processor,
            @Schema(description = "Оперативная память, ГБ", defaultValue = "8") @Min(1)
                    Integer ramGb,
            @Schema(description = "Память устройства, ГБ", defaultValue = "256") @Min(1)
                    Integer storageGb,
            @Schema(description = "Диагональ экрана, дюймы", defaultValue = "11")
                    @DecimalMin("0.001")
                    BigDecimal screenInches,
            @Schema(description = "Фронтальная камера, Мп", defaultValue = "12")
                    @DecimalMin("0.001")
                    BigDecimal frontCameraMp,
            @Schema(description = "Основная задняя камера, Мп", defaultValue = "50")
                    @DecimalMin("0.001")
                    @Digits(integer = 5, fraction = 3)
                    BigDecimal rearCameraMp,
            @Schema(description = "Ёмкость батареи, мА·ч", defaultValue = "8000") @Min(1)
                    Integer batteryMah,
            @Schema(description = "Поддержка SIM-карты", defaultValue = "false") Boolean cellular) {
        public ProductRequest toRequest() {
            Map<String, String> values =
                    computingValues(
                            processor,
                            ramGb,
                            storageGb,
                            screenInches == null ? BigDecimal.valueOf(11) : screenInches,
                            frontCameraMp);
            values.put("batteryCapacityMah", Integer.toString(or(batteryMah, 8000)));
            values.put("cellular", Boolean.toString(Boolean.TRUE.equals(cellular)));
            return request(
                    name, brand, price, ProductCategory.TABLET, values, camera(rearCameraMp, 50));
        }
    }

    @Schema(name = "LaptopForm")
    public record Laptop(
            @Schema(description = "Название товара", example = "Новый товар")
                    @NotBlank
                    @Size(max = 200)
                    String name,
            @Schema(
                            description = "Бренд. Новый бренд будет создан автоматически",
                            defaultValue = "DemoBrand")
                    @Size(max = 100)
                    String brand,
            @Schema(description = "Цена в рублях", example = "30000")
                    @NotNull
                    @DecimalMin("0.01")
                    @Digits(integer = 7, fraction = 2)
                    BigDecimal price,
            @Schema(description = "Процессор", defaultValue = "Demo CPU") @Size(max = 1000)
                    String processor,
            @Schema(description = "Оперативная память, ГБ", defaultValue = "8") @Min(1)
                    Integer ramGb,
            @Schema(description = "Память устройства, ГБ", defaultValue = "256") @Min(1)
                    Integer storageGb,
            @Schema(description = "Диагональ экрана, дюймы", defaultValue = "15.6")
                    @DecimalMin("0.001")
                    BigDecimal screenInches,
            @Schema(description = "Фронтальная камера, Мп", defaultValue = "12")
                    @DecimalMin("0.001")
                    BigDecimal frontCameraMp,
            @Schema(description = "Тип накопителя", defaultValue = "SSD") StorageType storageType) {
        public ProductRequest toRequest() {
            Map<String, String> values =
                    computingValues(
                            processor,
                            ramGb,
                            storageGb,
                            screenInches == null ? BigDecimal.valueOf(15.6) : screenInches,
                            frontCameraMp);
            values.put("storageType", (storageType == null ? StorageType.SSD : storageType).name());
            return request(name, brand, price, ProductCategory.LAPTOP, values, List.of());
        }
    }

    @Schema(name = "HeadphonesForm")
    public record Headphones(
            @Schema(description = "Название товара", example = "Новый товар")
                    @NotBlank
                    @Size(max = 200)
                    String name,
            @Schema(
                            description = "Бренд. Новый бренд будет создан автоматически",
                            defaultValue = "DemoBrand")
                    @Size(max = 100)
                    String brand,
            @Schema(description = "Цена в рублях", example = "30000")
                    @NotNull
                    @DecimalMin("0.01")
                    @Digits(integer = 7, fraction = 2)
                    BigDecimal price,
            @Schema(description = "Тип наушников", defaultValue = "OVER_EAR")
                    HeadphoneType headphoneType,
            @Schema(description = "Беспроводные", defaultValue = "true") Boolean wireless,
            @Schema(description = "Шумоподавление", defaultValue = "false") Boolean noiseCancelling,
            @Schema(description = "Минимальная частота, Гц", defaultValue = "20") @Min(1)
                    Integer minFrequencyHz,
            @Schema(description = "Максимальная частота, Гц", defaultValue = "20000") @Min(1)
                    Integer maxFrequencyHz) {
        public ProductRequest toRequest() {
            Map<String, String> values =
                    Map.of(
                            "headphoneType",
                            (headphoneType == null ? HeadphoneType.OVER_EAR : headphoneType).name(),
                            "wireless",
                            Boolean.toString(wireless == null || wireless),
                            "noiseCancelling",
                            Boolean.toString(Boolean.TRUE.equals(noiseCancelling)),
                            "minFrequencyHz",
                            Integer.toString(or(minFrequencyHz, 20)),
                            "maxFrequencyHz",
                            Integer.toString(or(maxFrequencyHz, 20000)));
            return request(name, brand, price, ProductCategory.HEADPHONES, values, List.of());
        }
    }

    @Schema(name = "ProductDetailsForm")
    public record Details(
            @Schema(description = "Новое название. Пустое поле — оставить прежнее") @Size(max = 200)
                    String name,
            @Schema(description = "Другой существующий бренд. Пустое поле — оставить прежний")
                    @Size(max = 100)
                    String brand,
            @Schema(description = "Новая цена. Пустое поле — оставить прежнюю")
                    @DecimalMin("0.01")
                    @Digits(integer = 7, fraction = 2)
                    BigDecimal price,
            @Schema(description = "Доступен для заказа. Не выбрано — оставить прежнее")
                    Boolean available) {
        public Details {
            name = blankToNull(name);
            brand = blankToNull(brand);
        }
    }

    private static Map<String, String> computingValues(
            String processor, Integer ram, Integer storage, BigDecimal screen, BigDecimal front) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("processor", text(processor, "Demo CPU"));
        values.put("ramGb", Integer.toString(or(ram, 8)));
        values.put("storageGb", Integer.toString(or(storage, 256)));
        values.put(
                "screenDiagonalInches",
                (screen == null ? BigDecimal.valueOf(6.5) : screen).toPlainString());
        values.put(
                "frontCameraMp", (front == null ? BigDecimal.valueOf(12) : front).toPlainString());
        return values;
    }

    private static ProductRequest request(
            String name,
            String brand,
            BigDecimal price,
            ProductCategory category,
            Map<String, String> values,
            List<ProductRequest.RearCameraRequest> cameras) {
        return new ProductRequest(
                name, text(brand, "DemoBrand"), category, price, true, values, cameras);
    }

    private static List<ProductRequest.RearCameraRequest> camera(
            BigDecimal resolution, int fallback) {
        return List.of(
                new ProductRequest.RearCameraRequest(
                        CameraType.WIDE,
                        resolution == null ? BigDecimal.valueOf(fallback) : resolution,
                        1));
    }

    private static int or(Integer value, int fallback) {
        return value == null ? fallback : value;
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.strip();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
