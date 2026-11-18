package com.example.techstore.service;

import com.example.techstore.dto.ReferenceDtos.BrandDto;
import com.example.techstore.dto.ReferenceDtos.CategoryDto;
import com.example.techstore.dto.ReferenceDtos.CharacteristicDto;

import java.util.List;

/** Операции со справочниками и связями характеристик. */
public interface ReferenceService {
    List<BrandDto> brands();

    BrandDto brand(String name);

    BrandDto createBrand(BrandDto request);

    BrandDto renameBrand(String old, BrandDto request);

    void deleteBrand(String name);

    List<CategoryDto> categories();

    List<CharacteristicDto> specifications();

    List<String> categorySpecifications(String code);
}
