package com.example.techstore.model.product;

import java.math.BigDecimal;
import java.util.Objects;

public final class Laptop extends ComputingDevice {
    private final FrontCamera frontCamera;
    private final StorageType storageType;

    public Laptop(long article, String name, String brand, BigDecimal price,
                  String processor, int ramGb, int storageGb, double screenDiagonalInches,
                  StorageType storageType, FrontCamera frontCamera) {
        super(article, name, brand, price, processor, ramGb, storageGb, screenDiagonalInches);
        this.frontCamera = Objects.requireNonNull(frontCamera, "frontCamera");
        this.storageType = Objects.requireNonNull(storageType, "storageType");
    }

    public FrontCamera getFrontCamera() { return frontCamera; }
    public StorageType getStorageType() { return storageType; }

    @Override
    public ProductCategory getCategory() { return ProductCategory.LAPTOP; }

    @Override
    public String getSpecifications() {
        return super.getSpecifications()
                + ", Тип накопителя: " + storageType
                + ", фронтальная камера (1): " + frontCamera.getSpecifications();
    }
}
