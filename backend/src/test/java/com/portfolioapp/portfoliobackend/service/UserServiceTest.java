package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.ProfileUpdateRequest;
import com.portfolioapp.portfoliobackend.dto.UserDto;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import com.portfolioapp.portfoliobackend.mapper.UserMapper;
import com.portfolioapp.portfoliobackend.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private UserService userService;

    private UserProfile testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = new UserProfile();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setRole("USER");
        testUser.setDescription("Old desc");
        testUser.setAvatarUrl("old.jpg");

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setEmail("test@example.com");
        testUserDto.setFullName("Test User");
    }

    @Test
    void getCurrentUser_ShouldReturnUserDto_WhenExists() {
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getCurrentUser("test@example.com");

        assertNotNull(result);
        assertEquals("Test User", result.getFullName());
        verify(userProfileRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    void getCurrentUser_ShouldThrowException_WhenNotFound() {
        when(userProfileRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.getCurrentUser("unknown@example.com")
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void updateProfile_ShouldUpdateAllFields() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("New Name");
        request.setDescription("New desc");
        request.setAvatarUrl("new.jpg");

        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUser);
        when(userMapper.toDto(any(UserProfile.class))).thenReturn(testUserDto);

        UserDto result = userService.updateProfile("test@example.com", request);

        assertNotNull(result);
        verify(userProfileRepository, times(1)).save(any(UserProfile.class));
    }

    @Test
    void updateProfile_ShouldKeepOldValues_WhenFieldsAreNull() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFullName("Only Name"); // остальные null

        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(testUser);
        when(userMapper.toDto(any(UserProfile.class))).thenReturn(testUserDto);

        userService.updateProfile("test@example.com", request);

        // Проверяем, что описание и аватар не затерлись (логика внутри сервиса)
        assertEquals("Old desc", testUser.getDescription());
        assertEquals("old.jpg", testUser.getAvatarUrl());
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUserNotFound() {
        when(userProfileRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());
        ProfileUpdateRequest request = new ProfileUpdateRequest();

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.updateProfile("unknown@example.com", request)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenExists() {
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        verify(userProfileRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_ShouldThrowException_WhenNotFound() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userService.getUserById(99L)
        );
        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        when(userProfileRepository.findAll()).thenReturn(Collections.singletonList(testUser));
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        verify(userProfileRepository, times(1)).findAll();
    }
}