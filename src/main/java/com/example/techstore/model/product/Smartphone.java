package com.example.techstore.model.product;

import com.example.techstore.model.Validation;
import java.math.BigDecimal;
import java.util.Objects;

public final class Smartphone extends ComputingDevice {
    private final CameraSetup cameras;
    private final int batteryCapacityMah;
    private final boolean supports5g;

    public Smartphone(long article, String name, String brand, BigDecimal price,
                      String processor, int ramGb, int storageGb, double screenDiagonalInches,
                      int batteryCapacityMah, boolean supports5g, CameraSetup cameras) {
        super(article, name, brand, price, processor, ramGb, storageGb, screenDiagonalInches);
        this.cameras = Objects.requireNonNull(cameras, "cameras");
        this.batteryCapacityMah = Validation.positive(batteryCapacityMah, "batteryCapacityMah");
        this.supports5g = supports5g;
    }

    public CameraSetup getCameras() { return cameras; }
    public int getBatteryCapacityMah() { return batteryCapacityMah; }
    public boolean supports5g() { return supports5g; }

    @Override
    public ProductCategory getCategory() { return ProductCategory.SMARTPHONE; }

    @Override
    public String getSpecifications() {
        return super.getSpecifications() + ", " + cameras.getSpecifications()
                + ", Батарея: " + batteryCapacityMah + " мА·ч"
                + ", 5G: " + (supports5g ? "да" : "нет");
    }
}
