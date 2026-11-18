package com.example.techstore.service;

import com.example.techstore.dto.UserDtos;
import com.example.techstore.model.user.UserRole;

import java.util.List;

/** Создание, чтение, обновление и удаление пользователей. */
public interface UserService {
    List<UserDtos.Response> list(UserRole role);

    UserDtos.Response get(String email);

    UserDtos.Response create(UserDtos.Create request);

    UserDtos.Response update(String email, UserDtos.Update request);

    void delete(String email);
}
