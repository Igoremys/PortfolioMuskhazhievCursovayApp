package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.ProfileUpdateRequest;
import com.portfolioapp.portfoliobackend.dto.UserDto;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import com.portfolioapp.portfoliobackend.mapper.UserMapper;
import com.portfolioapp.portfoliobackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserProfileRepository userProfileRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto updateProfile(String email, ProfileUpdateRequest request) {
        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getDescription() != null) user.setDescription(request.getDescription());

        return userMapper.toDto(userProfileRepository.save(user));
    }

    @Override
    public UserDto getCurrentUser(String email) {
        UserProfile user = userProfileRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserProfile> users = userProfileRepository.findAll();
        return users.stream().map(userMapper::toDto).toList();
    }

    @Override
    public List<UserDto> searchUsers(String keyword) {
        List<UserProfile> users = userProfileRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        return users.stream().map(userMapper::toDto).toList();
    }

    @Override
    public UserDto getUserById(Long id) {
        UserProfile user = userProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        return userMapper.toDto(user);
    }
}
