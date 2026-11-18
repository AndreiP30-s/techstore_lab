package com.example.techstore.model.user;

import com.example.techstore.model.AssignedIdEntity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "app_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "role", discriminatorType = DiscriminatorType.STRING, length = 10)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class User extends AssignedIdEntity {
    @Id
    @Column(length = 254)
    private String email;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "password_hash", nullable = false, columnDefinition = "text")
    private String passwordHash;

    protected User(String email, String name, String passwordHash) {
        this.email = email;
        this.name = name;
        this.passwordHash = passwordHash;
    }

    @Override
    public String getId() {
        return email;
    }

    @Transient
    public abstract UserRole getRole();

    public static User create(UserRole role, String email, String name, String passwordHash) {
        return switch (role) {
            case ADMIN -> new AdminUser(email, name, passwordHash);
            case MANAGER -> new ManagerUser(email, name, passwordHash);
            case CUSTOMER -> new CustomerUser(email, name, passwordHash);
        };
    }
}
