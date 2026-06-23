package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.ProfileUpdateRequest;
import com.portfolioapp.portfoliobackend.dto.UserDto;
import java.util.List;

public interface IUserService {
    UserDto updateProfile(String email, ProfileUpdateRequest request);
    UserDto getCurrentUser(String email);

    List<UserDto> getAllUsers();

    List<UserDto> searchUsers(String keyword);
    UserDto getUserById(Long id);
}
