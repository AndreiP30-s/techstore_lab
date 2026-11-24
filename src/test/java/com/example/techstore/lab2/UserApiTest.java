package com.example.techstore.lab2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

@ExtendWith(OutputCaptureExtension.class)
@WithMockUser(roles = "ADMIN")
class UserApiTest extends DatabaseFreeTest {
    @Autowired MockMvc mvc;
    String json =
            "{\"email\":\"Test@example.com\",\"name\":\"Test\",\"password\":\"Password123\",\"role\":\"CUSTOMER\"}";

    User user() {
        return User.create(UserRole.CUSTOMER, "test@example.com", "Test", "secret");
    }

    @org.junit.jupiter.api.BeforeEach
    void prepareRoleUpdate() {
        when(users.changeRole(anyString(), anyString())).thenReturn(1);
    }

    @Test
    void hashesPasswordAndDoesNotExposeIt(CapturedOutput output) throws Exception {
        mvc.perform(post("/api/users").contentType("application/json").content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).saveAndFlush(saved.capture());
        assertTrue(
                new BCryptPasswordEncoder()
                        .matches("Password123", saved.getValue().getPasswordHash()));
        assertThat(output).contains("Создан пользователь: email=test@example.com, роль=CUSTOMER")
                .doesNotContain("Password123", saved.getValue().getPasswordHash());
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        when(users.existsById("test@example.com")).thenReturn(true);
        mvc.perform(post("/api/users").contentType("application/json").content(json))
                .andExpect(status().isConflict());
    }

    @Test
    void readsListsUpdatesAndDeletes(CapturedOutput output) throws Exception {
        User entity = user();
        when(users.findById("test@example.com")).thenReturn(Optional.of(entity));
        when(users.findByRoleName("CUSTOMER")).thenReturn(List.of(entity));
        mvc.perform(get("/api/users/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(get("/api/users?role=CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"));
        mvc.perform(
                        put("/api/users/test@example.com")
                                .contentType("application/json")
                                .content("{\"name\":\"Manager\",\"role\":\"MANAGER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("MANAGER"));
        mvc.perform(delete("/api/users/test@example.com")).andExpect(status().isNoContent());
        verify(users).delete(entity);
        assertThat(output).contains("Изменена роль пользователя: email=test@example.com, прежняя=CUSTOMER, новая=MANAGER");
    }

    @Test
    void handlesInvalidAndMissingUser() throws Exception {
        mvc.perform(get("/api/users/absent@example.com")).andExpect(status().isNotFound());
        mvc.perform(
                        post("/api/users")
                                .contentType("application/json")
                                .content(json.replace("Password123", "123")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createsConcreteJpaSubtypeForEveryRole() {
        for (UserRole role : UserRole.values()) {
            User user = User.create(role, "test@example.com", "Name", "hash");
            assertEquals(role, user.getRole());
            assertEquals("test@example.com", user.getId());
            assertTrue(user.isNew());
        }
        assertInstanceOf(
                com.example.techstore.model.user.AdminUser.class,
                User.create(UserRole.ADMIN, "a@example.com", "Admin", "hash"));
        assertInstanceOf(
                com.example.techstore.model.user.ManagerUser.class,
                User.create(UserRole.MANAGER, "m@example.com", "Manager", "hash"));
        assertInstanceOf(com.example.techstore.model.user.CustomerUser.class, user());
    }
}
