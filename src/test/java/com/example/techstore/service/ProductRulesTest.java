package com.example.techstore.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.techstore.exception.ValidationException;
import com.example.techstore.dto.ProductRequest;
import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.ProductCategory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ProductRulesTest {
    private final ProductRules rules = new ProductRules();

    private ProductRequest valid(ProductCategory category) {
        Map<String, String> values = new HashMap<>();
        List<ProductRequest.RearCameraRequest> cameras = List.of();
        if (category == ProductCategory.HEADPHONES) {
            values.putAll(
                    Map.of(
                            "headphoneType",
                            "OVER_EAR",
                            "wireless",
                            "true",
                            "noiseCancelling",
                            "false",
                            "minFrequencyHz",
                            "20",
                            "maxFrequencyHz",
                            "20000"));
        } else {
            values.putAll(
                    Map.of(
                            "processor",
                            "CPU",
                            "ramGb",
                            "8",
                            "storageGb",
                            "256",
                            "screenDiagonalInches",
                            "6.5",
                            "frontCameraMp",
                            "12"));
            switch (category) {
                case SMARTPHONE ->
                        values.putAll(Map.of("batteryCapacityMah", "5000", "supports5g", "true"));
                case TABLET ->
                        values.putAll(Map.of("batteryCapacityMah", "8000", "cellular", "true"));
                case LAPTOP -> values.put("storageType", "SSD");
                default -> throw new IllegalArgumentException();
            }
            if (category != ProductCategory.LAPTOP)
                cameras =
                        List.of(
                                new ProductRequest.RearCameraRequest(
                                        CameraType.WIDE, new BigDecimal("50"), 1));
        }
        return new ProductRequest(
                "Product", "Brand", category, new BigDecimal("100"), true, values, cameras);
    }

    private ProductRequest with(
            ProductRequest request,
            Map<String, String> values,
            List<ProductRequest.RearCameraRequest> cameras) {
        return new ProductRequest(
                request.name(),
                request.brand(),
                request.category(),
                request.price(),
                request.available(),
                values,
                cameras);
    }

    @ParameterizedTest
    @EnumSource(ProductCategory.class)
    void acceptsAllCategories(ProductCategory category) {
        assertDoesNotThrow(() -> rules.validate(valid(category)));
    }

    @ParameterizedTest
    @EnumSource(ProductCategory.class)
    void requiresEveryMandatoryField(ProductCategory category) {
        ProductRequest request = valid(category);
        for (String field : ProductRules.required(category)) {
            Map<String, String> values = new HashMap<>(request.specifications());
            values.remove(field);
            assertThrows(
                    ValidationException.class,
                    () -> rules.validate(with(request, values, request.rearCameras())),
                    field);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "ramGb,0",
        "ramGb,-1",
        "ramGb,8.5",
        "ramGb,2147483648",
        "storageGb,0",
        "screenDiagonalInches,0",
        "screenDiagonalInches,-1",
        "screenDiagonalInches,NaN",
        "frontCameraMp,0",
        "batteryCapacityMah,-1",
        "supports5g,yes"
    })
    void rejectsInvalidComputingFields(String field, String value) {
        ProductRequest request = valid(ProductCategory.SMARTPHONE);
        Map<String, String> values = new HashMap<>(request.specifications());
        values.put(field, value);
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, values, request.rearCameras())));
    }

    @Test
    void rejectsUnknownEnumsAndInvalidFrequencyRange() {
        ProductRequest request = valid(ProductCategory.HEADPHONES);
        Map<String, String> values = new HashMap<>(request.specifications());
        values.put("headphoneType", "UNKNOWN");
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, values, List.of())));
        values.put("headphoneType", "IN_EAR");
        values.put("minFrequencyHz", "20000");
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, values, List.of())));
        ProductRequest laptop = valid(ProductCategory.LAPTOP);
        Map<String, String> laptopValues = new HashMap<>(laptop.specifications());
        laptopValues.put("storageType", "UNKNOWN");
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(laptop, laptopValues, List.of())));
    }

    @Test
    void rejectsMissingDuplicateExcessAndInapplicableCameras() {
        ProductRequest request = valid(ProductCategory.SMARTPHONE);
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, request.specifications(), List.of())));
        ProductRequest.RearCameraRequest camera = request.rearCameras().get(0);
        assertThrows(
                ValidationException.class,
                () ->
                        rules.validate(
                                with(request, request.specifications(), List.of(camera, camera))));
        List<ProductRequest.RearCameraRequest> excess =
                List.of(
                        new ProductRequest.RearCameraRequest(CameraType.WIDE, BigDecimal.TEN, 16),
                        new ProductRequest.RearCameraRequest(CameraType.MACRO, BigDecimal.TEN, 16),
                        new ProductRequest.RearCameraRequest(
                                CameraType.TELEPHOTO, BigDecimal.TEN, 1));
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, request.specifications(), excess)));
        ProductRequest laptop = valid(ProductCategory.LAPTOP);
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(laptop, laptop.specifications(), List.of(camera))));
    }

    @Test
    void treatsEquivalentDecimalResolutionsAsSameCameraGroup() {
        ProductRequest request = valid(ProductCategory.SMARTPHONE);
        List<ProductRequest.RearCameraRequest> cameras =
                List.of(
                        new ProductRequest.RearCameraRequest(
                                CameraType.WIDE, new BigDecimal("50.000"), 1),
                        new ProductRequest.RearCameraRequest(
                                CameraType.WIDE, new BigDecimal("50"), 1));
        assertThrows(
                ValidationException.class,
                () -> rules.validate(with(request, request.specifications(), cameras)));
    }
}
