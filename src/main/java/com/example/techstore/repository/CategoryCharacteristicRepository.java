package com.example.techstore.repository;

import com.example.techstore.model.catalog.CategoryCharacteristic;
import com.example.techstore.model.catalog.CategoryCharacteristicId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryCharacteristicRepository
        extends JpaRepository<CategoryCharacteristic, CategoryCharacteristicId> {
    List<CategoryCharacteristic> findByIdCategoryCode(String code);

    boolean existsByIdSpecificationName(String name);
}
