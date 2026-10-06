package com.example.techstore.service;
import org.springframework.stereotype.Service;
import com.example.techstore.model.Validation;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.repository.UserRepository;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
@Service
public class UserService {
    private final UserRepository repository;
    public UserService(UserRepository repository) { this.repository = repository; }
    public synchronized void add(User user) {
        Objects.requireNonNull(user, "user");
        if (findByEmail(user.getEmail()).isPresent()) { throw new IllegalArgumentException("Email already exists"); }
        repository.add(user);
    }
    public Optional<User> findByEmail(String email) {
        String normalized = Validation.text(email, "email").toLowerCase(Locale.ROOT);
        for (User user : repository.findAll()) {
            if (user.getEmail().equals(normalized)) { return Optional.of(user); }
        }
        return Optional.empty();
    }
    public List<User> findByRole(UserRole role) {
        Objects.requireNonNull(role, "role");
        List<User> result = new ArrayList<>();
        for (User user : repository.findAll()) {
            if (user.getRole() == role) { result.add(user); }
        }
        return List.copyOf(result);
    }
    public List<User> findAll() { return repository.findAll(); }
    public boolean delete(UUID id) { return repository.deleteById(id); }
}
