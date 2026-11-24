package com.example.techstore.controller;

import com.example.techstore.dto.UserCreateForm;
import com.example.techstore.dto.UserDtos.Create;
import com.example.techstore.dto.UserDtos.Response;
import com.example.techstore.dto.UserDtos.Update;
import com.example.techstore.model.user.UserRole;
import com.example.techstore.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Tag(name = "2. Пользователи")
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = UserCreateForm.class)))
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Добавить пользователя — заполнить поля")
    public ResponseEntity<Response> createForm(
            @Parameter(hidden = true) @Valid @ModelAttribute
                    UserCreateForm form) {
        return create(form.toRequest());
    }

    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content =
                    @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                            schema =
                                    @io.swagger.v3.oas.annotations.media.Schema(
                                            implementation = Update.class)))
    @PutMapping(value = "/{email}/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "Изменить пользователя — заполнить поля")
    public Response updateForm(
            @Parameter(description = "Email существующего пользователя", example = "customer@example.com")
                    @PathVariable String email,
            @Parameter(hidden = true) @Valid @ModelAttribute
                    Update form) {
        return service.update(email, form);
    }

    @GetMapping
    @Operation(summary = "Список пользователей и фильтр по роли")
    public List<Response> list(
            @Parameter(description = "Роль для фильтрации списка", example = "CUSTOMER")
                    @RequestParam(required = false) UserRole role) {
        return service.list(role);
    }

    @GetMapping("/{email}")
    @Operation(summary = "Найти пользователя по email")
    public Response get(
            @Parameter(description = "Email существующего пользователя", example = "customer@example.com")
                    @PathVariable String email) {
        return service.get(email);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать пользователя")
    public ResponseEntity<Response> create(@Valid @RequestBody Create request) {
        Response response = service.create(request);
        return ResponseEntity.created(
                        UriComponentsBuilder.fromPath("/api/users/{email}")
                                .buildAndExpand(response.email())
                                .encode()
                                .toUri())
                .body(response);
    }

    @PutMapping("/{email}")
    @Operation(summary = "Изменить имя и роль")
    public Response update(
            @Parameter(description = "Email существующего пользователя", example = "customer@example.com")
                    @PathVariable String email,
            @Valid @RequestBody Update request) {
        return service.update(email, request);
    }

    @DeleteMapping("/{email}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пользователя")
    public void delete(
            @Parameter(description = "Email существующего пользователя", example = "customer@example.com")
                    @PathVariable String email) {
        service.delete(email);
    }
}
