package com.example.techstore.config;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.techstore.dto.UserDtos;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.repository.UserRepository;
import com.example.techstore.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class AdminBootstrapTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserService service = mock(UserService.class);
    private final DefaultApplicationArguments args = new DefaultApplicationArguments();

    @Test
    void skipsBootstrapWithoutExplicitPassword() {
        new AdminBootstrap(users, service, "admin@example.com", "").run(args);
        verifyNoInteractions(users, service);
    }

    @Test
    void leavesExistingUserUntouched() {
        when(users.existsById("admin@example.com")).thenReturn(true);
        new AdminBootstrap(users, service, "Admin@example.com", "TemporaryPass18").run(args);
        verifyNoInteractions(service);
    }

    @Test
    void delegatesAdminCreationToExistingUserService() {
        new AdminBootstrap(users, service, "Admin@example.com", "TemporaryPass18").run(args);
        verify(service).create(new UserDtos.Create(
                "admin@example.com", "Администратор", "TemporaryPass18", UserRole.ADMIN));
    }
}
