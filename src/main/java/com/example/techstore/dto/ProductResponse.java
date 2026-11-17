package com.example.techstore.dto;

import com.example.techstore.model.product.CameraType;
import com.example.techstore.model.product.Product;
import com.example.techstore.model.product.ProductCategory;
import com.example.techstore.model.product.ProductSpecification;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public record ProductResponse(
        Integer article,
        String name,
        String brand,
        ProductCategory category,
        BigDecimal price,
        boolean available,
        Map<String, String> specifications,
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
