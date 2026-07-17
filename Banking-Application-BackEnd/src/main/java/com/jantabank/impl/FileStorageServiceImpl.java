package com.jantabank.impl;

import com.jantabank.exception.ResourceNotFoundException;
import com.jantabank.exception.TodoAPIException;
import com.jantabank.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path baseDir;

    public FileStorageServiceImpl(@Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.baseDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize storage directory: " + this.baseDir, e);
        }
    }

    @Override
    public String store(byte[] content, String subDirectory, String originalFilename) {
        if (content == null || content.length == 0) {
            throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }
        try {
            Path dir = baseDir.resolve(subDirectory).normalize();
            if (!dir.startsWith(baseDir)) {
                throw new TodoAPIException(HttpStatus.BAD_REQUEST, "Invalid storage path");
            }
            Files.createDirectories(dir);
            String ext = extensionOf(originalFilename);
            String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(fileName);
            Files.write(target, content);
            String relative = baseDir.relativize(target).toString().replace('\\', '/');
            log.info("Stored file at {}", relative);
            return relative;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store file");
        }
    }

    @Override
    public byte[] load(String storagePath) {
        try {
            Path target = baseDir.resolve(storagePath).normalize();
            if (!target.startsWith(baseDir) || !Files.exists(target)) {
                throw new ResourceNotFoundException("File not found");
            }
            return Files.readAllBytes(target);
        } catch (IOException e) {
            throw new TodoAPIException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not read file");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Path target = baseDir.resolve(storagePath).normalize();
            if (target.startsWith(baseDir)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException e) {
            log.warn("Could not delete file {}", storagePath, e);
        }
    }

    private String extensionOf(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dot = cleaned.lastIndexOf('.');
        if (dot >= 0 && dot < cleaned.length() - 1) {
            String ext = cleaned.substring(dot);
            if (ext.matches("\\.[A-Za-z0-9]{1,8}")) {
                return ext.toLowerCase();
            }
        }
        return "";
    }
}
