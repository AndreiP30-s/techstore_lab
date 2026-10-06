package com.example.techstore.model.product;

import com.example.techstore.model.Validation;
import java.math.BigDecimal;
import java.util.Objects;

public final class Tablet extends ComputingDevice {
    private final CameraSetup cameras;
    private final int batteryCapacityMah;
    private final boolean cellular;

    public Tablet(long article, String name, String brand, BigDecimal price,
                  String processor, int ramGb, int storageGb, double screenDiagonalInches,
                  int batteryCapacityMah, boolean cellular, CameraSetup cameras) {
        super(article, name, brand, price, processor, ramGb, storageGb, screenDiagonalInches);
        this.cameras = Objects.requireNonNull(cameras, "cameras");
        this.batteryCapacityMah = Validation.positive(batteryCapacityMah, "batteryCapacityMah");
        this.cellular = cellular;
    }

    public CameraSetup getCameras() { return cameras; }
    public int getBatteryCapacityMah() { return batteryCapacityMah; }
    public boolean isCellular() { return cellular; }

    @Override
    public ProductCategory getCategory() { return ProductCategory.TABLET; }

    @Override
    public String getSpecifications() {
        return super.getSpecifications() + ", " + cameras.getSpecifications()
                + ", Батарея: " + batteryCapacityMah + " мА·ч"
                + ", Мобильная связь: " + (cellular ? "да" : "нет");
    }
}
