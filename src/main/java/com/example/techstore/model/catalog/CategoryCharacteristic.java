package com.example.techstore.model.catalog;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "category_specification")
@Getter
@Setter
@NoArgsConstructor
public class CategoryCharacteristic {
    @EmbeddedId private CategoryCharacteristicId id;

    public CategoryCharacteristic(String category, String specification) {
        id = new CategoryCharacteristicId(category, specification);
    }
}
