package com.example.techstore.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        @Schema(description = "HTTP-статус ответа", example = "404") int status,
        @Schema(description = "Стабильный код ошибки", example = "PRODUCT_NOT_FOUND") String error,
        @Schema(description = "Причина ошибки") String message,
        @Schema(description = "Подсказка, что можно сделать") String hint,
        @Schema(description = "Ошибки отдельных полей, если есть") List<String> details,
        @Schema(description = "Время ошибки в UTC") Instant timestamp) {}
