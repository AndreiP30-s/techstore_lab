package com.example.techstore.service.impl;

import com.example.techstore.dto.UserDtos;
import com.example.techstore.exception.ConflictException;
import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.NotFoundException;
import com.example.techstore.exception.ValidationException;
import com.example.techstore.model.user.User;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.repository.UserRepository;
import com.example.techstore.service.EmailNormalizer;
import com.example.techstore.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserServiceImpl(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public List<UserDtos.Response> list(UserRole role) {
        return (role == null ? users.findAll() : users.findByRoleName(role.name()))
                .stream().map(UserDtos.Response::from).toList();
    }

    @Override
    public UserDtos.Response get(String email) {
        return UserDtos.Response.from(require(email));
    }

    @Transactional
    @Override
    public UserDtos.Response create(UserDtos.Create request) {
        String email = EmailNormalizer.normalize(request.email());
        if (users.existsById(email))
            throw new ConflictException(ErrorCode.DUPLICATE_EMAIL, "Email уже занят");
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw new ValidationException(ErrorCode.PASSWORD_TOO_LONG, "Пароль не больше 72 байт UTF-8");
        User user =
                User.create(
                        request.role(),
                        email,
                        request.name().strip(),
                        encoder.encode(request.password()));
        users.saveAndFlush(user);
        return UserDtos.Response.from(user);
    }

    @Transactional
    @Override
    public UserDtos.Response update(String email, UserDtos.Update request) {
        User user = require(email);
        user.setName(request.name().strip());
        users.flush();
        if (user.getRole() != request.role()
                && users.changeRole(user.getEmail(), request.role().name()) != 1)
            throw new NotFoundException(ErrorCode.USER_NOT_FOUND, "Пользователь не найден");
        return new UserDtos.Response(user.getEmail(), user.getName(), request.role());
    }

    @Transactional
    @Override
    public void delete(String email) {
        users.delete(require(email));
        users.flush();
    }

    private User require(String email) {
        return users.findById(EmailNormalizer.normalize(email))
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND, "Пользователь не найден"));
    }
}
