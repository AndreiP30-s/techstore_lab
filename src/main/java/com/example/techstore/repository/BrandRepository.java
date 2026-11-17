package com.example.techstore.repository;

import com.example.techstore.model.catalog.Brand;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, String> {}
