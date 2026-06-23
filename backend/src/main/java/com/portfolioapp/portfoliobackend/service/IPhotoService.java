package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.PhotoDto;
import java.util.List;
import java.util.Optional;

public interface IPhotoService {
    List<PhotoDto> getAllPhotos(String userEmail);
    Optional<PhotoDto> getPhotoById(Long id);
    PhotoDto createPhoto(PhotoDto photoDto, String authorEmail);
    PhotoDto updatePhoto(Long id, PhotoDto photoDto, String authorEmail);
    void deletePhoto(Long id, String authorEmail);
    List<PhotoDto> searchPhotos(String keyword);

    List<PhotoDto> getPhotosByAuthorId(Long authorId, String currentUserEmail);

    PhotoDto toggleLike(Long photoId, String userEmail);
}
