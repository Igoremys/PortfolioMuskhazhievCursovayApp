package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.AuthRequest;
import com.portfolioapp.portfoliobackend.dto.AuthResponse;

public interface IAuthService {
    // Регистрация: принимаем весь запрос
    AuthResponse register(AuthRequest request);

    // Логин: принимаем весь запрос
    AuthResponse login(AuthRequest request);
}