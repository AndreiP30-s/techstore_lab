package com.example.techstore.security;

import com.example.techstore.exception.ErrorCode;
import com.example.techstore.exception.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);

    private final JsonMapper json;

    public SecurityErrorHandler(JsonMapper json) {
        this.json = json;
    }

    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        log.warn("Аутентификация отклонена: метод={}, путь={}",
                request.getMethod(), RequestAudit.safe(request.getRequestURI()));
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"TechStore\"");
        write(response, HttpStatus.UNAUTHORIZED, ErrorCode.AUTHENTICATION_REQUIRED,
                "Требуется вход с действительными учётными данными");
    }

    @Override
    public void handle(
            HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception)
            throws IOException {
        Principal principal = request.getUserPrincipal();
        log.warn("Доступ запрещён: пользователь={}, метод={}, путь={}",
                principal == null ? "анонимный" : RequestAudit.safe(principal.getName()),
                request.getMethod(), RequestAudit.safe(request.getRequestURI()));
        write(response, HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED,
                "Недостаточно прав для этой операции");
    }

    private void write(HttpServletResponse response, HttpStatus status, ErrorCode code, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        json.writeValue(response.getOutputStream(),
                new ErrorResponse(status.value(), code.name(), message, code.getHint(), List.of(), Instant.now()));
    }
}
