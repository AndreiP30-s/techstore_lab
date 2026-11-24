package com.example.techstore.dto;

import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.ProductSpecification;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record ProductResponse(
        @Schema(description = "Уникальный артикул товара", example = "100001") Integer article,
        @Schema(description = "Название товара", example = "Смартфон A1") String name,
        @Schema(description = "Название бренда", example = "DemoBrand") String brand,
        @Schema(description = "Категория товара", example = "SMARTPHONE") ProductCategory category,
        @Schema(description = "Цена в рублях", example = "24990.00") BigDecimal price,
        @Schema(description = "Товар доступен для заказа") boolean available,
        @Schema(description = "Характеристики товара в виде пар «название — значение»")
                Map<String, String> specifications,
        @Schema(description = "Группы задних камер, если они есть у товара")
                List<ProductRequest.RearCameraRequest> rearCameras) {
    public static ProductResponse from(Product product) {
        Map<String, String> values = new TreeMap<>();
        for (ProductSpecification value : product.getSpecifications())
            values.put(value.getId().getSpecificationName(), value.getValue());
        List<ProductRequest.RearCameraRequest> cameras =
                product.getRearCameras().stream()
                        .map(
                                camera ->
                                        new ProductRequest.RearCameraRequest(
                                                CameraType.valueOf(camera.getId().getCameraType()),
                                                camera.getId()
                                                        .getResolutionMp()
                                                        .stripTrailingZeros(),
                                                camera.getQuantity()))
                        .sorted(
                                Comparator.comparing(
                                        camera ->
                                                camera.type().name()
                                                        + ":"
                                                        + camera.resolutionMp().toPlainString()))
                        .toList();
        return new ProductResponse(
                product.getArticle(),
                product.getName(),
                product.getBrand().getName(),
                ProductCategory.valueOf(product.getCategory().getCode()),
                product.getPrice(),
                product.isAvailable(),
                values,
                cameras);
    }
}
