package com.example.techstore.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.util.Set;

@Component
public class RequestAudit implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RequestAudit.class);
    private static final Set<String> WRITE_METHODS = Set.of(
            HttpMethod.POST.name(), HttpMethod.PUT.name(), HttpMethod.PATCH.name(), HttpMethod.DELETE.name());

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        Principal principal = request.getUserPrincipal();
        int status = response.getStatus();
        if (exception == null && principal != null && status >= 200 && status < 300
                && WRITE_METHODS.contains(request.getMethod())) {
            log.info("Операция выполнена: пользователь={}, метод={}, путь={}, статус={}",
                    safe(principal.getName()), request.getMethod(), safe(request.getRequestURI()), status);
        }
    }

    static String safe(String value) {
        return value.replaceAll("[\\p{Cntrl}\\p{Zl}\\p{Zp}]", "_");
    }
}
