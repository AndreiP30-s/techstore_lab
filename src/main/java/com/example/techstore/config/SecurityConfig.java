package com.example.techstore.config;

import com.example.techstore.security.RequestAudit;
import com.example.techstore.security.SecurityErrorHandler;

import jakarta.servlet.DispatcherType;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.MappedInterceptor;

@Configuration
public class SecurityConfig {
    @Bean
    public MappedInterceptor requestAuditInterceptor(RequestAudit audit) {
        return new MappedInterceptor(new String[]{"/api/**"}, audit);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityErrorHandler errors) {
        String[] catalog = {"/api/products/**", "/api/brands/**"};
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.authenticationEntryPoint(errors))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errors)
                        .accessDeniedHandler(errors))
                .authorizeHttpRequests(requests -> requests
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, catalog).hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, catalog).hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, catalog).hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/discount").hasAnyRole("MANAGER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()
                        .anyRequest().denyAll())
                .build();
    }
}
