package com.example.learning.materials.application;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MaterialStorageKeyFactory {

    public String create(UUID userId, UUID subjectId, UUID materialId, String originalFileName) {
        return "users/%s/subjects/%s/materials/%s/%s".formatted(
                userId,
                subjectId,
                materialId,
                sanitize(originalFileName)
        );
    }

    private String sanitize(String fileName) {
        return fileName == null || fileName.isBlank()
                ? "upload.pdf"
                : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
