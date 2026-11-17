package com.example.techstore.model.user;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("CUSTOMER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomerUser extends User {
    public CustomerUser(String email, String name, String passwordHash) {
        super(email, name, passwordHash);
    }

    @Override
    public UserRole getRole() {
        return UserRole.CUSTOMER;
    }
}
