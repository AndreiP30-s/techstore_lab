package com.example.techstore.model.user;
import com.example.techstore.model.Identifiable;
import com.example.techstore.model.Validation;
import java.util.Objects;
import java.util.UUID;
public abstract class User implements Identifiable<UUID> {
    private final UUID id;
    private final String name;
    private final String email;
    protected User(UUID id, String name, String email) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Validation.text(name, "name");
        this.email = Validation.email(email);
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public abstract UserRole getRole();
    @Override public final boolean equals(Object other) {
        return this == other || other instanceof User user && id.equals(user.id);
    }
    @Override public final int hashCode() { return id.hashCode(); }
}
