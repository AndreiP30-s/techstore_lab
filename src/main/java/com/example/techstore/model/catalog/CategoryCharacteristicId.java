package com.example.techstore.model.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCharacteristicId implements Serializable {
    @Column(name = "category_code")
    private String categoryCode;

    @Column(name = "specification_name")
    private String specificationName;
}
