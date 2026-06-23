package com.portfolioapp.portfoliobackend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    // Папка для сохранения аватаров (относительно корня проекта)
    private static final String UPLOAD_DIR = "uploads/avatars/";

    public String saveAvatar(MultipartFile file, String username) throws IOException {
        // 1. Создаём уникальное имя файла, чтобы они не перезаписывали друг друга
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : ".jpg";

        String fileName = username + "_" + System.currentTimeMillis() + extension;

        // 2. Создаём папку, если её нет
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 3. Сохраняем файл на диск
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // 4. Возвращаем публичный URL для доступа из Android
        return "http://localhost:8080/api/photos/files/" + fileName;
    }
}
