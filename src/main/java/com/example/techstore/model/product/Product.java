package com.example.techstore.model.product;
import com.example.techstore.model.Identifiable;
import com.example.techstore.model.Validation;
import java.math.BigDecimal;
public abstract class Product implements Identifiable<Long> {
    private final long article;
    private final String name;
    private final String brand;
    private volatile BigDecimal price;
    protected Product(long article, String name, String brand, BigDecimal price) {
        this.article = Validation.article(article);
        this.name = Validation.text(name, "name");
        this.brand = Validation.text(brand, "brand");
        this.price = Validation.price(price);
    }
    /** Ключ репозитория — артикул товара. */
    @Override public Long getId() { return article; }
    public long getArticle() { return article; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public BigDecimal getPrice() { return price; }
    public void changePrice(BigDecimal price) { this.price = Validation.price(price); }
    public abstract ProductCategory getCategory();
    public abstract String getSpecifications();
    @Override public final boolean equals(Object other) {
        return this == other || other instanceof Product product && article == product.article;
    }
    @Override public final int hashCode() { return Long.hashCode(article); }
}
