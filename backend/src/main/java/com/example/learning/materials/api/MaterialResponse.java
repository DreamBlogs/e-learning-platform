package com.example.learning.materials.api;

import com.example.learning.materials.domain.Material;
import com.example.learning.materials.domain.MaterialStatus;
import java.time.Instant;
import java.util.UUID;

public record MaterialResponse(
        UUID id,
        UUID subjectId,
        String fileName,
        String fileType,
        String storageKey,
        long sizeBytes,
        MaterialStatus status,
        String errorMessage,
        Instant createdAt,
        Instant processedAt
) {
    public static MaterialResponse from(Material material) {
        return new MaterialResponse(
                material.getId(),
                material.getSubjectId(),
                material.getFileName(),
                material.getFileType(),
                material.getStorageKey(),
                material.getSizeBytes(),
                material.getStatus(),
                material.getErrorMessage(),
                material.getCreatedAt(),
                material.getProcessedAt()
        );
    }
}
