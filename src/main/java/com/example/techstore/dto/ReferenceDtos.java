package com.example.techstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ReferenceDtos {
    private ReferenceDtos() {}

    public record BrandDto(
            @Schema(description = "Название бренда", example = "DemoBrand")
                    @NotBlank @Size(max = 100) String name) {}

    public record CategoryDto(
            @Schema(description = "Код категории", example = "SMARTPHONE") String code,
            @Schema(description = "Название категории", example = "Смартфоны") String name) {}

    public record CharacteristicDto(
            @Schema(description = "Ключ характеристики", example = "ramGb")
                    @NotBlank @Size(max = 100) String name,
            @Schema(description = "Тип значения характеристики", example = "INTEGER")
                    @NotNull @Pattern(regexp = "INTEGER|DECIMAL|TEXT|BOOLEAN|ENUM") String valueType,
            @Schema(description = "Единица измерения, если применима", example = "GB")
                    @Size(max = 20) String unit) {}
}
