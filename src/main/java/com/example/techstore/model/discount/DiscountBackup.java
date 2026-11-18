package com.example.techstore.model.discount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "discount_backup")
@Getter
@Setter
@NoArgsConstructor
public class DiscountBackup {
    @Id
    @Column(name = "product_article")
    private Integer productArticle;

    @Column(name = "original_price", nullable = false, precision = 9, scale = 2)
    private BigDecimal originalPrice;

    public DiscountBackup(Integer article, BigDecimal price) {
        productArticle = article;
        originalPrice = price;
    }
}
