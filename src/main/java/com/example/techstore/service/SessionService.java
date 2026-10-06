package com.example.techstore.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final UserService users;
    private User currentUser;

    public SessionService(UserService users) { this.users = users; }

    public List<User> availableUsers() { return users.findAll(); }

    public void select(UUID id) {
        currentUser = null;
        for (User user : users.findAll()) {
            if (user.getId().equals(id)) {
                currentUser = user;
                return;
            }
        }
        throw new IllegalArgumentException("Пользователь не найден.");
    }

    public Optional<User> currentUser() { return Optional.ofNullable(currentUser); }
    public void clear() { currentUser = null; }

    public User requireUser() {
        return currentUser().orElseThrow(() -> new SecurityException("Сначала выберите пользователя."));
    }

    public void requireRole(UserRole... allowed) {
        User user = requireUser();
        for (UserRole role : allowed) {
            if (user.getRole() == role) { return; }
        }
        throw new SecurityException("Недостаточно прав для этого действия.");
    }
}
