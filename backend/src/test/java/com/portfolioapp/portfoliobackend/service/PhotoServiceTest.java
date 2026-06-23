package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.PhotoDto;
import com.portfolioapp.portfoliobackend.entity.Photo;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import com.portfolioapp.portfoliobackend.mapper.PhotoMapper;
import com.portfolioapp.portfoliobackend.repository.PhotoRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock private PhotoRepository photoRepository;
    @Mock private UserProfileRepository userProfileRepository;
    @Mock private PhotoMapper photoMapper;
    @InjectMocks private PhotoService photoService;

    private Photo testPhoto;
    private UserProfile testUser;
    private PhotoDto testPhotoDto;

    @BeforeEach
    void setUp() {
        testUser = new UserProfile();
        testUser.setId(10L);
        testUser.setEmail("test@example.com");

        testPhoto = new Photo();
        testPhoto.setId(1L);
        testPhoto.setTitle("Test Photo");
        testPhoto.setAuthor("test@example.com");
        testPhoto.setUserProfile(testUser);

        testPhotoDto = new PhotoDto();
        testPhotoDto.setId(1L);
        testPhotoDto.setTitle("Test Photo");
    }

    @Test
    void createPhoto_ShouldSavePhotoSuccessfully() {
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(photoMapper.toEntity(any(PhotoDto.class))).thenReturn(testPhoto);
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);
        when(photoMapper.toDto(any(Photo.class), any(UserProfile.class))).thenReturn(testPhotoDto);

        PhotoDto result = photoService.createPhoto(testPhotoDto, "test@example.com");

        assertNotNull(result);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void createPhoto_ShouldThrowException_WhenAuthorNotFound() {
        when(userProfileRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                photoService.createPhoto(testPhotoDto, "unknown@example.com")
        );
        assertTrue(exception.getMessage().contains("Автор не найден"));
    }

    @Test
    void getPhotoById_ShouldReturnPhoto_WhenExists() {
        when(photoRepository.findByIdWithLikes(1L)).thenReturn(Optional.of(testPhoto));
        when(photoMapper.toDto(any(Photo.class), any())).thenReturn(testPhotoDto);

        Optional<PhotoDto> result = photoService.getPhotoById(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void getPhotoById_ShouldReturnEmpty_WhenNotFound() {
        when(photoRepository.findByIdWithLikes(99L)).thenReturn(Optional.empty());

        Optional<PhotoDto> result = photoService.getPhotoById(99L);

        assertFalse(result.isPresent());
    }

    @Test
    void updatePhoto_ShouldUpdateSuccessfully_WhenIsOwner() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(photoMapper.toDto(any(Photo.class), any())).thenReturn(testPhotoDto);

        PhotoDto result = photoService.updatePhoto(1L, testPhotoDto, "test@example.com");

        assertNotNull(result);
        verify(photoRepository, times(1)).save(any(Photo.class));
    }

    @Test
    void updatePhoto_ShouldThrowSecurityException_WhenNotOwner() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        SecurityException exception = assertThrows(SecurityException.class, () ->
                photoService.updatePhoto(1L, testPhotoDto, "hacker@example.com")
        );
        assertEquals("Вы не автор этой фотографии!", exception.getMessage());
    }

    @Test
    void deletePhoto_ShouldDeleteSuccessfully_WhenIsOwner() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        photoService.deletePhoto(1L, "test@example.com");

        verify(photoRepository, times(1)).delete(testPhoto);
    }

    @Test
    void deletePhoto_ShouldThrowSecurityException_WhenNotOwner() {
        when(photoRepository.findById(1L)).thenReturn(Optional.of(testPhoto));

        SecurityException exception = assertThrows(SecurityException.class, () ->
                photoService.deletePhoto(1L, "hacker@example.com")
        );
        assertEquals("Вы не автор этой фотографии!", exception.getMessage());
    }

    @Test
    void toggleLike_ShouldAddLike_WhenNotLikedYet() {
        when(photoRepository.findByIdWithLikes(1L)).thenReturn(Optional.of(testPhoto));
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(photoMapper.toDto(any(), any())).thenReturn(testPhotoDto);
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        photoService.toggleLike(1L, "test@example.com");

        assertTrue(testPhoto.isLikedBy(testUser));
        verify(photoRepository, times(1)).save(testPhoto);
    }

    @Test
    void toggleLike_ShouldRemoveLike_WhenAlreadyLiked() {
        testPhoto.addLike(testUser); // Сначала добавляем лайк
        when(photoRepository.findByIdWithLikes(1L)).thenReturn(Optional.of(testPhoto));
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(photoMapper.toDto(any(), any())).thenReturn(testPhotoDto);
        when(photoRepository.save(any(Photo.class))).thenReturn(testPhoto);

        photoService.toggleLike(1L, "test@example.com");

        assertFalse(testPhoto.isLikedBy(testUser));
        verify(photoRepository, times(1)).save(testPhoto);
    }

    @Test
    void getAllPhotos_ShouldReturnListWithCurrentUser() {
        when(photoRepository.findAllWithLikes()).thenReturn(Collections.singletonList(testPhoto));
        when(userProfileRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(photoMapper.toDto(any(Photo.class), any(UserProfile.class))).thenReturn(testPhotoDto);

        List<PhotoDto> result = photoService.getAllPhotos("test@example.com");

        assertEquals(1, result.size());
    }

    @Test
    void searchPhotos_ShouldReturnMatchingPhotos() {
        when(photoRepository.searchByKeyword("Test")).thenReturn(Collections.singletonList(testPhoto));
        when(photoMapper.toDto(any(Photo.class), any())).thenReturn(testPhotoDto);

        List<PhotoDto> result = photoService.searchPhotos("Test");

        assertEquals(1, result.size());
    }
}