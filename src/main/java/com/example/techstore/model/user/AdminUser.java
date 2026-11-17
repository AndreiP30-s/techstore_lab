package com.example.techstore.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("ADMIN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUser extends User {
    public AdminUser(String email, String name, String passwordHash) {
        super(email, name, passwordHash);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}
