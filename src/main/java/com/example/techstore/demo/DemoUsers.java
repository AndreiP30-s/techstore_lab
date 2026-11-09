package com.example.techstore.demo;

import java.util.UUID;
import com.example.techstore.model.user.AdminUser;
import com.example.techstore.model.user.ManagerUser;
import com.example.techstore.model.user.CustomerUser;
import com.example.techstore.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-1)
public class DemoUsers implements CommandLineRunner {
    private final UserService users;
    public DemoUsers(UserService users) { this.users = users; }

    @Override
    public void run(String... args) {
        users.add(new AdminUser(UUID.randomUUID(), "Администратор", "admin@example.org"));
        users.add(new ManagerUser(UUID.randomUUID(), "Менеджер", "manager@example.org"));
        users.add(new CustomerUser(UUID.randomUUID(), "Покупатель", "customer@example.org"));
    }
}
