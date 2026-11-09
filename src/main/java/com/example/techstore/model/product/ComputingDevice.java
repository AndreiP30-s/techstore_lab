package com.example.techstore.model.product;

import com.example.techstore.model.Validation;
import java.math.BigDecimal;

public abstract class ComputingDevice extends Product {
    private final String processor;
    private final int ramGb;
    private final int storageGb;
    private final double screenDiagonalInches;

    protected ComputingDevice(long article, String name, String brand, BigDecimal price,
                              String processor, int ramGb, int storageGb, double screenDiagonalInches) {
        super(article, name, brand, price);
        this.processor = Validation.text(processor, "processor");
        this.ramGb = Validation.positive(ramGb, "ramGb");
        this.storageGb = Validation.positive(storageGb, "storageGb");
        this.screenDiagonalInches = Validation.positive(screenDiagonalInches, "screenDiagonalInches");
    }

    public String getProcessor() { return processor; }
    public int getRamGb() { return ramGb; }
    public int getStorageGb() { return storageGb; }
    public double getScreenDiagonalInches() { return screenDiagonalInches; }

    @Override
    public String getSpecifications() {
        return "Процессор: " + processor + ", ОЗУ: " + ramGb + " ГБ, накопитель: "
                + storageGb + " ГБ, экран: " + screenDiagonalInches + " дюйма";
    }
}
