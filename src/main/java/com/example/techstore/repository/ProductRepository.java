package com.example.techstore.repository;

import com.example.techstore.model.product.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
        extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    boolean existsByBrandName(String name);

    boolean existsBySpecificationsIdSpecificationName(String name);
}
