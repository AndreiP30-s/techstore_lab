package com.example.techstore.dto;

import com.example.techstore.model.user.UserRole;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.*;

@Schema(name = "UserCreateForm")
public record UserCreateForm(
        @Schema(description = "Email пользователя", example = "customer@example.com")
                @NotBlank
                @Email
                @Size(max = 254)
                String email,
        @Schema(description = "Имя пользователя", example = "Покупатель") @NotBlank @Size(max = 100)
                String name,
        @Schema(
                        description = "Пароль от 8 символов",
                        format = "password",
                        accessMode = Schema.AccessMode.WRITE_ONLY)
                @NotBlank
                @Size(min = 8, max = 72)
                String password,
        @Schema(description = "Роль. CUSTOMER — покупатель", defaultValue = "CUSTOMER")
                UserRole role) {
    public UserDtos.Create toRequest() {
        return new UserDtos.Create(email, name, password, role == null ? UserRole.CUSTOMER : role);
    }
}
