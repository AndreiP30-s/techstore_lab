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

import java.math.BigDecimal;

@Entity
@Table(name = "rear_camera")
@Getter
@Setter
@NoArgsConstructor
public class RearCamera {
    @EmbeddedId private RearCameraId id;

    @MapsId("productArticle")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_article")
    private Product product;

    @Column(nullable = false)
    private int quantity;

    public RearCamera(Product product, String type, BigDecimal resolution, int quantity) {
        this.product = product;
        this.id = new RearCameraId(product.getArticle(), type, resolution.setScale(3));
        this.quantity = quantity;
    }
}
