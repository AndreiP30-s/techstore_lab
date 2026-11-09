package com.example.techstore;
import org.junit.jupiter.api.Test;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.model.user.AdminUser;
import com.example.techstore.model.user.ManagerUser;
import com.example.techstore.model.user.CustomerUser;
import com.example.techstore.repository.memory.InMemoryUserRepository;
import com.example.techstore.service.UserService;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class UserServiceTest {
    // Добавление пользователей разных ролей, поиск по email и удаление.
    @Test void addsAllRolesAndFindsNormalizedEmail() {
        UserService service = new UserService(new InMemoryUserRepository());
        User admin = new AdminUser(UUID.randomUUID(), "Anna", " ADMIN@example.org ");
        User manager = new ManagerUser(UUID.randomUUID(), "Ivan", "manager@example.org");
        User customer = new CustomerUser(UUID.randomUUID(), "Maria", "customer@example.org");
        for (User user : List.of(admin, manager, customer)) { service.add(user); }
        assertEquals(admin, service.findByEmail(" Admin@EXAMPLE.org ").orElseThrow());
        assertEquals(List.of(admin), service.findByRole(UserRole.ADMIN));
        assertEquals(List.of(manager), service.findByRole(UserRole.MANAGER));
        assertEquals(List.of(customer), service.findByRole(UserRole.CUSTOMER));
        assertTrue(service.delete(customer.getId()));
        assertFalse(service.delete(customer.getId()));
        assertTrue(service.findByRole(UserRole.CUSTOMER).isEmpty());
    }
    // Повторные email и UUID отклоняются.
    @Test void rejectsDuplicateEmailAndId() {
        UserService service = new UserService(new InMemoryUserRepository());
        UUID id = UUID.randomUUID();
        service.add(new CustomerUser(id, "A", "a@example.org"));
        assertThrows(IllegalArgumentException.class, () -> service.add(new AdminUser(UUID.randomUUID(), "B", "A@EXAMPLE.ORG")));
        assertThrows(IllegalArgumentException.class, () -> service.add(new ManagerUser(id, "B", "b@example.org")));
        assertEquals(1, service.findAll().size());
    }
}
