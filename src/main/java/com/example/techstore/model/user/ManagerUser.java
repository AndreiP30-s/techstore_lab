package com.example.techstore.model.user;
import java.util.UUID;
public final class ManagerUser extends User {
    public ManagerUser(UUID id, String name, String email) { super(id, name, email); }
    @Override public UserRole getRole() { return UserRole.MANAGER; }
}
