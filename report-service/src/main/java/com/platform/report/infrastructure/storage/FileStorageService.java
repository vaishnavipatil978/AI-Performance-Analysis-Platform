package com.platform.report.infrastructure.storage;

import com.platform.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.storage.upload-dir:/uploads}") String uploadDirPath) {
        this.uploadDir = Paths.get(uploadDirPath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + uploadDirPath, e);
        }
    }

    public String store(MultipartFile file, UUID userId) {
        if (file.isEmpty()) {
            throw new BusinessException("File must not be empty", HttpStatus.BAD_REQUEST, "EMPTY_FILE");
        }
        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String fileName = userId + "/" + UUID.randomUUID() + extension;
        try {
            Path targetDir = uploadDir.resolve(userId.toString());
            Files.createDirectories(targetDir);
            Path targetPath = uploadDir.resolve(fileName).normalize();
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Stored file: {}", targetPath);
            return targetPath.toString();
        } catch (IOException e) {
            throw new BusinessException("Failed to store file: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE_ERROR");
        }
    }

    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }
}
