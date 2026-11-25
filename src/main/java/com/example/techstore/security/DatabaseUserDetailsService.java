package com.example.techstore.security;

import com.example.techstore.exception.ValidationException;
import com.example.techstore.model.user.User;
import com.example.techstore.repository.UserRepository;
import com.example.techstore.service.EmailNormalizer;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public DatabaseUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String email;
        try {
            email = EmailNormalizer.normalize(username);
        } catch (ValidationException exception) {
            throw new UsernameNotFoundException("Пользователь не найден");
        }
        User user = users.findById(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
