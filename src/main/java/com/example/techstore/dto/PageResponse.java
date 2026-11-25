package com.example.techstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        @Schema(description = "Записи на текущей странице") List<T> content,
        @Schema(description = "Номер текущей страницы, начиная с нуля", example = "0") int page,
        @Schema(description = "Запрошенный размер страницы", example = "20") int size,
        @Schema(description = "Общее число записей с учётом фильтров", example = "47")
                long totalElements,
        @Schema(description = "Число страниц при выбранном размере страницы", example = "3")
                int totalPages) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
