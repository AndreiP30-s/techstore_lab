package com.example.techstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class ReferenceDtos {
    private ReferenceDtos() {}

    public record BrandDto(@NotBlank @Size(max = 100) String name) {}

    public record CategoryDto(String code, String name) {}

    public record CharacteristicDto(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Pattern(regexp = "INTEGER|DECIMAL|TEXT|BOOLEAN|ENUM") String valueType,
            @Size(max = 20) String unit) {}
}
