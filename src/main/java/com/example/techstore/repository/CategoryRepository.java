package com.example.techstore.repository;

import com.example.techstore.model.catalog.Category;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, String> {}
