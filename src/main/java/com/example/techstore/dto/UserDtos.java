package com.example.techstore.dto;

import com.example.techstore.model.user.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class UserDtos {
    private UserDtos() {}

    @Schema(
            name = "UserCreateRequest",
            example =
                    """
{"email":"customer@example.com","name":"Покупатель","password":"DemoPassword123","role":"CUSTOMER"}
""")
    public record Create(
            @Schema(description = "Email для входа", example = "customer@example.com")
                    @NotBlank @Email @Size(max = 254) String email,
            @Schema(description = "Имя пользователя") @NotBlank @Size(max = 100) String name,
            @Schema(description = "Пароль для входа, максимум 72 байта в UTF-8",
                    accessMode = Schema.AccessMode.WRITE_ONLY)
                    @NotBlank @Size(min = 8, max = 72) String password,
            @Schema(description = "Назначаемая роль", example = "CUSTOMER")
                    @NotNull UserRole role) {}

    @Schema(name = "UserUpdateRequest")
    public record Update(
            @Schema(description = "Новое имя пользователя") @NotBlank @Size(max = 100) String name,
            @Schema(description = "Новая роль пользователя", example = "MANAGER")
                    @NotNull UserRole role) {}

    @Schema(name = "UserResponse")
    public record Response(
            @Schema(description = "Email пользователя", example = "customer@example.com") String email,
            @Schema(description = "Имя пользователя") String name,
            @Schema(description = "Текущая роль", example = "CUSTOMER") UserRole role) {
        public static Response from(com.example.techstore.model.user.User user) {
            return new Response(user.getEmail(), user.getName(), user.getRole());
        }
    }
}
