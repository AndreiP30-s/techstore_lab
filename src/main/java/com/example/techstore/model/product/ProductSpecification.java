package com.example.techstore.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_specification")
@Getter
@Setter
@NoArgsConstructor
public class ProductSpecification {
    @EmbeddedId private ProductSpecificationId id;

    @MapsId("productArticle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_article")
    private Product product;

    @Column(nullable = false, columnDefinition = "text")
    private String value;

    public ProductSpecification(Product product, String name, String value) {
        this.product = product;
        this.id = new ProductSpecificationId(product.getArticle(), name);
        this.value = value;
    }
}
