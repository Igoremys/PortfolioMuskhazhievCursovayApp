package com.portfolioapp.portfoliobackend.controller;

import com.portfolioapp.portfoliobackend.dto.PhotoDto;
import com.portfolioapp.portfoliobackend.service.IPhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Photos", description = "Операции с фотографиями")
public class PhotoController {

    private final IPhotoService photoService;
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    @GetMapping
    @Operation(summary = "Получить все фотографии")
    public ResponseEntity<List<PhotoDto>> getAllPhotos(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(photoService.getAllPhotos(userEmail));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить фотографию по ID")
    public ResponseEntity<PhotoDto> getPhotoById(@PathVariable("id") Long id) {
        return photoService.getPhotoById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Создать новую фотографию (JSON)")
    public ResponseEntity<PhotoDto> createPhoto(
            @RequestBody PhotoDto photoDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        PhotoDto created = photoService.createPhoto(photoDto, userDetails.getUsername());
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить фотографию")
    public ResponseEntity<PhotoDto> updatePhoto(
            @PathVariable("id") Long id,
            @RequestBody PhotoDto photoDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(photoService.updatePhoto(id, photoDto, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить фотографию")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        try {
            log.info("🗑️ DELETE запрос: id={}, userDetails={}", id,
                    userDetails != null ? userDetails.getUsername() : "NULL");

            if (userDetails == null) {
                log.error(" userDetails is null — пользователь не аутентифицирован");
                return ResponseEntity.status(401).build();
            }

            String authorEmail = userDetails.getUsername();
            if (authorEmail == null || authorEmail.isEmpty()) {
                log.error(" Username is null/empty");
                return ResponseEntity.status(401).build();
            }

            photoService.deletePhoto(id, authorEmail);
            log.info(" Фото {} удалено пользователем {}", id, authorEmail);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            log.warn(" Запрет доступа: {}", e.getMessage());
            return ResponseEntity.status(403).build();
        } catch (RuntimeException e) {
            log.error(" Ошибка: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error(" Внутренняя ошибка при удалении фото {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Поставить/убрать лайк")
    public ResponseEntity<PhotoDto> toggleLike(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            log.warn(" Попытка лайка без авторизации");
            return ResponseEntity.status(401).build();
        }

        try {
            String userEmail = userDetails.getUsername();
            PhotoDto result = photoService.toggleLike(id, userEmail);
            log.info(" Лайк переключён: photo={}, user={}", id, userEmail);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error(" Ошибка при лайке: {}", e.getMessage());
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            log.error(" Внутренняя ошибка при лайке: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск фотографий")
    public ResponseEntity<List<PhotoDto>> searchPhotos(@RequestParam String keyword) {
        return ResponseEntity.ok(photoService.searchPhotos(keyword));
    }

    @PostMapping("/upload")
    @Operation(summary = "Загрузить фото и получить URL")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Файл пуст"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Разрешены только изображения"));
        }

        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
            String ext = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
            String fileName = UUID.randomUUID().toString() + ext;

            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info(" Фото загружено: {} пользователем: {}", fileName, userDetails.getUsername());

            String fileUrl = "http://192.168.0.33:8080/api/photos/files/" + fileName;
            return ResponseEntity.ok(Map.of("url", fileUrl, "message", "Успешно загружено"));

        } catch (IOException e) {
            log.error(" Ошибка сохранения файла", e);
            return ResponseEntity.status(500).body(Map.of("error", "Ошибка сервера при сохранении"));
        }
    }

    @GetMapping("/files/{filename}")
    @Operation(summary = "Получить загруженное фото")
    public ResponseEntity<Resource> serveFile(@PathVariable("filename") String filename) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            Path filePath = uploadPath.resolve(filename).normalize();

            log.info(" Запрос файла: {}", filePath);

            if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
                log.error(" Файл не найден: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            log.info(" Файл отправлен: {}, тип: {}", filename, contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .body(resource);

        } catch (MalformedURLException e) {
            log.error(" Некорректный путь к файлу: {}", filename, e);
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error(" Ошибка чтения файла: {}", filename, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    //  НОВОЕ: Получение фотографий конкретного автора по ID
    @GetMapping("/by-author")
    public ResponseEntity<List<PhotoDto>> getPhotosByAuthorId(
            @RequestParam("authorId") Long authorId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = (userDetails != null) ? userDetails.getUsername() : null;
        return ResponseEntity.ok(photoService.getPhotosByAuthorId(authorId, userEmail));
    }
}
