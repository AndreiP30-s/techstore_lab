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
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 100) String name,
            @Schema(accessMode = Schema.AccessMode.WRITE_ONLY) @NotBlank @Size(min = 8, max = 72)
                    String password,
            @NotNull UserRole role) {}

    @Schema(name = "UserUpdateRequest")
    public record Update(@NotBlank @Size(max = 100) String name, @NotNull UserRole role) {}

    @Schema(name = "UserResponse")
    public record Response(String email, String name, UserRole role) {
        public static Response from(com.example.techstore.model.user.User user) {
            return new Response(user.getEmail(), user.getName(), user.getRole());
        }
    }
}
