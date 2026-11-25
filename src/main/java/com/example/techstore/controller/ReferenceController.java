package com.example.techstore.controller;

import com.example.techstore.dto.ReferenceDtos.BrandDto;
import com.example.techstore.dto.ReferenceDtos.CategoryDto;
import com.example.techstore.dto.ReferenceDtos.CharacteristicDto;
import com.example.techstore.service.ReferenceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "3. Справочники")
@RestController
@RequestMapping("/api")
public class ReferenceController {
    private final ReferenceService service;

    public ReferenceController(ReferenceService service) {
        this.service = service;
    }

    @GetMapping("/brands")
    @Operation(summary = "Список брендов")
    public List<BrandDto> brands() {
        return service.brands();
    }

    @GetMapping("/brands/{name}")
    @Operation(summary = "Найти бренд по имени")
    public BrandDto brand(
            @Parameter(description = "Точное название бренда", example = "DemoBrand")
                    @PathVariable String name) {
        return service.brand(name);
    }

    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать бренд")
    public BrandDto createBrand(@Valid @RequestBody BrandDto request) {
        return service.createBrand(request);
    }

    @PutMapping("/brands/{name}")
    @Operation(summary = "Переименовать неиспользуемый бренд")
    public BrandDto renameBrand(
            @Parameter(description = "Текущее название бренда", example = "DemoBrand")
                    @PathVariable String name,
            @Valid @RequestBody BrandDto request) {
        return service.renameBrand(name, request);
    }

    @DeleteMapping("/brands/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить неиспользуемый бренд")
    public void deleteBrand(
            @Parameter(description = "Точное название бренда", example = "DemoBrand")
                    @PathVariable String name) {
        service.deleteBrand(name);
    }

    @GetMapping("/categories")
    @Operation(summary = "Список категорий")
    public List<CategoryDto> categories() {
        return service.categories();
    }

    @GetMapping("/specifications")
    @Operation(summary = "Список характеристик")
    public List<CharacteristicDto> specifications() {
        return service.specifications();
    }

    @GetMapping("/categories/{code}/specifications")
    @Operation(summary = "Характеристики категории")
    public List<String> categorySpecifications(
            @Parameter(description = "Код категории из справочника", example = "SMARTPHONE")
                    @PathVariable String code) {
        return service.categorySpecifications(code);
    }
}
