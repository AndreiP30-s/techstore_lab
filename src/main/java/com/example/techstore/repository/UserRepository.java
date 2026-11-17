package com.example.techstore.repository;

import com.example.techstore.model.user.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    @Query(value = "SELECT * FROM app_user WHERE role=:role ORDER BY email", nativeQuery = true)
    List<User> findByRoleName(@Param("role") String role);

    // Смена роли меняет подкласс JPA-сущности. Сначала сохраняем изменения,
    // затем очищаем контекст, чтобы при чтении загрузился нужный подкласс.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "UPDATE app_user SET role=:role WHERE email=:email", nativeQuery = true)
    int changeRole(@Param("email") String email, @Param("role") String role);
}
