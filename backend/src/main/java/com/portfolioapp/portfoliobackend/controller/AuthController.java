package com.portfolioapp.portfoliobackend.controller;

import com.portfolioapp.portfoliobackend.dto.AuthRequest;
import com.portfolioapp.portfoliobackend.dto.AuthResponse;
import com.portfolioapp.portfoliobackend.service.IAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Операции аутентификации")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация нового пользователя")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        // Просто передаём request в сервис
        return ResponseEntity.status(201).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Аутентификация пользователя")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        // Просто передаём request в сервис
        return ResponseEntity.ok(authService.login(request));
    }
}