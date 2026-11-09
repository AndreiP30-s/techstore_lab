package com.example.techstore.model.product;
import java.util.Objects;
import com.example.techstore.model.Validation;
import java.math.BigDecimal;
public final class Headphones extends Product {
    private final int minFrequencyHz;
    private final int maxFrequencyHz;
    private final HeadphoneType type;
    private final boolean wireless;
    private final boolean noiseCancelling;
    public Headphones(long article, String name, String brand, BigDecimal price, HeadphoneType type, boolean wireless, boolean noiseCancelling, int minFrequencyHz, int maxFrequencyHz) {
        super(article, name, brand, price);
        this.minFrequencyHz = Validation.positive(minFrequencyHz, "minFrequencyHz");
        this.maxFrequencyHz = Validation.positive(maxFrequencyHz, "maxFrequencyHz");
        if (minFrequencyHz >= maxFrequencyHz) {
            throw new IllegalArgumentException("Minimum frequency must be lower than maximum frequency");
        }
        this.type = Objects.requireNonNull(type, "type");
        this.wireless = wireless;
        this.noiseCancelling = noiseCancelling;
    }
    public int getMinFrequencyHz() { return minFrequencyHz; }
    public int getMaxFrequencyHz() { return maxFrequencyHz; }
    public HeadphoneType getType() { return type; }
    public boolean isWireless() { return wireless; }
    public boolean isNoiseCancelling() { return noiseCancelling; }
    @Override public ProductCategory getCategory() { return ProductCategory.HEADPHONES; }
    @Override public String getSpecifications() { return "Тип: " + type.getDisplayName() + ", беспроводные: " + (wireless ? "да" : "нет")
            + ", диапазон частот: " + minFrequencyHz + "–" + maxFrequencyHz + " Гц"
            + ", активное шумоподавление: " + (noiseCancelling ? "да" : "нет"); }
}
