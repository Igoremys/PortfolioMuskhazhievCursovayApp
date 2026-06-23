package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.AuthRequest;
import com.portfolioapp.portfoliobackend.dto.AuthResponse;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import com.portfolioapp.portfoliobackend.mapper.UserMapper;
import com.portfolioapp.portfoliobackend.repository.UserProfileRepository;
import com.portfolioapp.portfoliobackend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final UserProfileRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserMapper userMapper;

    @Override
    public AuthResponse register(AuthRequest request) {
        // 1. Проверяем, не занят ли email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Ошибка: Email уже зарегистрирован!");
        }

        // 2. Создаем сущность пользователя
        UserProfile user = UserProfile.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName()) // Берём fullName из запроса
                .role("USER")
                .build();

        // 3. Сохраняем в БД
        UserProfile savedUser = userRepository.save(user);

        // 4. Генерируем токен
        String token = jwtTokenProvider.generateToken(savedUser.getEmail());

        return AuthResponse.builder()
                .accessToken(token)
                .user(userMapper.toDto(savedUser))
                .build();
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // 1. Аутентификация через Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Установка контекста безопасности
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Генерация токена
        String token = jwtTokenProvider.generateToken(authentication.getName());

        // 4. Формирование ответа
        UserProfile user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        return AuthResponse.builder()
                .accessToken(token)
                .user(userMapper.toDto(user))
                .build();
    }
}