package com.example.techstore.repository;

import com.example.techstore.model.catalog.Characteristic;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacteristicRepository extends JpaRepository<Characteristic, String> {}
