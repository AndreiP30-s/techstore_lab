package com.example.techstore.config;

import com.example.techstore.dto.UserDtos;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.repository.UserRepository;
import com.example.techstore.service.EmailNormalizer;
import com.example.techstore.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository users;
    private final UserService service;
    private final String email;
    private final String password;

    public AdminBootstrap(
            UserRepository users, UserService service,
            @Value("${ADMIN_EMAIL:}") String email, @Value("${ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.service = service;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            if (!email.isBlank() || !password.isBlank()) {
                log.warn("Для создания стартового администратора задайте ADMIN_EMAIL и ADMIN_PASSWORD");
            }
            return;
        }
        String normalizedEmail = EmailNormalizer.normalize(email);
        if (users.existsById(normalizedEmail)) {
            return;
        }
        service.create(new UserDtos.Create(normalizedEmail, "Администратор", password, UserRole.ADMIN));
        log.info("Создан стартовый администратор");
    }
}
