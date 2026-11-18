package com.example.techstore.model.product;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RearCameraId implements Serializable {
    @Column(name = "product_article")
    private Integer productArticle;

    @Column(name = "camera_type")
    private String cameraType;

    @Column(name = "resolution_mp", precision = 8, scale = 3)
    private BigDecimal resolutionMp;
}
