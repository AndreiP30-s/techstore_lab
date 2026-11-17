package com.example.techstore.controller;

import com.example.techstore.dto.ReferenceDtos.BrandDto;
import com.example.techstore.dto.ReferenceDtos.CategoryDto;
import com.example.techstore.dto.ReferenceDtos.CharacteristicDto;
import com.example.techstore.service.ReferenceService;

import io.swagger.v3.oas.annotations.Operation;
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
    public BrandDto brand(@PathVariable String name) {
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
    public BrandDto renameBrand(@PathVariable String name, @Valid @RequestBody BrandDto request) {
        return service.renameBrand(name, request);
    }

    @DeleteMapping("/brands/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить неиспользуемый бренд")
    public void deleteBrand(@PathVariable String name) {
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

    @GetMapping("/specifications/{name}")
    @Operation(summary = "Найти характеристику")
    public CharacteristicDto specification(@PathVariable String name) {
        return service.specification(name);
    }

    @PostMapping("/specifications")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать характеристику")
    public CharacteristicDto createSpecification(@Valid @RequestBody CharacteristicDto request) {
        return service.createSpecification(request);
    }

    @PutMapping("/specifications/{name}")
    @Operation(summary = "Обновить характеристику")
    public CharacteristicDto updateSpecification(
            @PathVariable String name, @Valid @RequestBody CharacteristicDto request) {
        return service.updateSpecification(name, request);
    }

    @DeleteMapping("/specifications/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить характеристику")
    public void deleteSpecification(@PathVariable String name) {
        service.deleteSpecification(name);
    }

    @GetMapping("/categories/{code}/specifications")
    @Operation(summary = "Характеристики категории")
    public List<String> categorySpecifications(@PathVariable String code) {
        return service.categorySpecifications(code);
    }

    @PutMapping("/categories/{code}/specifications/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Разрешить характеристику для категории")
    public void link(@PathVariable String code, @PathVariable String name) {
        service.link(code, name);
    }

    @DeleteMapping("/categories/{code}/specifications/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Убрать характеристику из категории")
    public void unlink(@PathVariable String code, @PathVariable String name) {
        service.unlink(code, name);
    }
}
