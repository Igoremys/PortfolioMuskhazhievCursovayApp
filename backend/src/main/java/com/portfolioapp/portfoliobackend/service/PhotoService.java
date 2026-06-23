package com.portfolioapp.portfoliobackend.service;

import com.portfolioapp.portfoliobackend.dto.PhotoDto;
import com.portfolioapp.portfoliobackend.entity.Photo;
import com.portfolioapp.portfoliobackend.entity.UserProfile;
import com.portfolioapp.portfoliobackend.mapper.PhotoMapper;
import com.portfolioapp.portfoliobackend.repository.PhotoRepository;
import com.portfolioapp.portfoliobackend.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhotoService implements IPhotoService {

    private final PhotoRepository photoRepository;
    private final UserProfileRepository userRepository;
    private final PhotoMapper photoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getAllPhotos(String userEmail) {
        log.info("📸 Запрос всех фото из БД, пользователь: {}", userEmail);
        List<Photo> photos = photoRepository.findAllWithLikes();
        log.info(" Найдено фото в БД: {}", photos.size());

        final UserProfile currentUser;
        if (userEmail != null && !userEmail.isEmpty()) {
            currentUser = userRepository.findByEmail(userEmail).orElse(null);
        } else {
            currentUser = null;
        }

        return photos.stream()
                .map(photo -> photoMapper.toDto(photo, currentUser))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PhotoDto> getPhotoById(Long id) {
        log.info(" Запрос фото по ID: {}", id);
        return photoRepository.findByIdWithLikes(id)
                .map(photo -> photoMapper.toDto(photo, null));
    }

    @Override
    @Transactional
    public PhotoDto createPhoto(PhotoDto photoDto, String authorEmail) {
        log.info(" Создание фото: title={}, author={}", photoDto.getTitle(), authorEmail);

        UserProfile author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> {
                    log.error(" Автор не найден: {}", authorEmail);
                    return new RuntimeException("Автор не найден: " + authorEmail);
                });

        Photo photo = photoMapper.toEntity(photoDto);
        photo.setAuthor(authorEmail);
        photo.setUserProfile(author);

        Photo savedPhoto = photoRepository.save(photo);
        return photoMapper.toDto(savedPhoto, author);
    }

    @Override
    @Transactional
    public PhotoDto updatePhoto(Long id, PhotoDto photoDto, String authorEmail) {
        log.info(" Обновление фото: id={}, author={}", id, authorEmail);

        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(" Фотография не найдена: {}", id);
                    return new RuntimeException("Фотография не найдена: " + id);
                });

        if (!safeEquals(photo.getAuthor(), authorEmail)) {
            log.warn(" Попытка изменить чужое фото");
            throw new SecurityException("Вы не автор этой фотографии!");
        }

        photoMapper.updateEntityFromDto(photoDto, photo);
        Photo updatedPhoto = photoRepository.save(photo);
        return photoMapper.toDto(updatedPhoto, userRepository.findByEmail(authorEmail).orElse(null));
    }

    @Override
    @Transactional
    public void deletePhoto(Long id, String authorEmail) {
        log.info(" Удаление фото: id={}, author={}", id, authorEmail);

        if (authorEmail == null || authorEmail.isEmpty()) {
            log.error(" authorEmail is null/empty");
            throw new SecurityException("Пользователь не аутентифицирован");
        }

        Photo photo = photoRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(" Фотография не найдена: {}", id);
                    return new RuntimeException("Фотография не найдена: " + id);
                });

        if (!safeEquals(photo.getAuthor(), authorEmail)) {
            log.warn(" Попытка удалить чужое фото");
            throw new SecurityException("Вы не автор этой фотографии!");
        }

        photoRepository.delete(photo);
        log.info(" Фото удалено: id={}", id);
    }

    @Override
    @Transactional
    public PhotoDto toggleLike(Long photoId, String userEmail) {
        log.info(" Toggle like: photoId={}, user={}", photoId, userEmail);

        Photo photo = photoRepository.findByIdWithLikes(photoId)
                .orElseThrow(() -> {
                    log.error(" Фото не найдено: {}", photoId);
                    return new RuntimeException("Фото не найдено");
                });

        UserProfile user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error(" Пользователь не найден: {}", userEmail);
                    return new RuntimeException("Пользователь не найден");
                });

        if (photo.isLikedBy(user)) {
            photo.removeLike(user);
            log.info(" Лайк убран: photo={}, user={}", photoId, userEmail);
        } else {
            photo.addLike(user);
            log.info(" Лайк добавлен: photo={}, user={}", photoId, userEmail);
        }

        Photo savedPhoto = photoRepository.save(photo);
        return photoMapper.toDto(savedPhoto, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> searchPhotos(String keyword) {
        log.info(" Поиск фото по ключевому слову: '{}'", keyword);
        List<Photo> photos = photoRepository.searchByKeyword(keyword);
        return photos.stream()
                .map(photo -> photoMapper.toDto(photo, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getPhotosByAuthorId(Long authorId, String currentUserEmail) {
        log.info("👤 Запрос фото по authorId: {}, currentUser: {}", authorId, currentUserEmail);

        List<Photo> photos = photoRepository.findByUserProfileIdOrderByCreatedAtDesc(authorId);

        final UserProfile currentUser;
        if (currentUserEmail != null && !currentUserEmail.isEmpty()) {
            currentUser = userRepository.findByEmail(currentUserEmail).orElse(null);
        } else {
            currentUser = null;
        }

        return photos.stream()
                .map(photo -> photoMapper.toDto(photo, currentUser)) 
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PhotoDto> getPhotosByAuthorEmail(String authorEmail) {
        log.info("👤 Запрос фото по authorEmail: {}", authorEmail);
        List<Photo> photos = photoRepository.findByAuthorWithLikes(authorEmail);
        return photos.stream()
                .map(photo -> photoMapper.toDto(photo, null))
                .collect(Collectors.toList());
    }

    private boolean safeEquals(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equals(str2);
    }
}
