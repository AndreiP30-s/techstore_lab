package com.example.techstore.model.user;
import java.util.UUID;
public final class CustomerUser extends User {
    public CustomerUser(UUID id, String name, String email) { super(id, name, email); }
    @Override public UserRole getRole() { return UserRole.CUSTOMER; }
}
