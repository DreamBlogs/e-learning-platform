package com.example.learning.materials.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "file_name", nullable = false, length = 512)
    private String fileName;

    @Column(name = "file_type", nullable = false, length = 120)
    private String fileType;

    @Column(name = "storage_key", nullable = false, length = 1024)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private MaterialStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    protected Material() {
    }

    public Material(UUID subjectId, UUID userId, String fileName, String fileType, String storageKey, long sizeBytes) {
        this(UUID.randomUUID(), subjectId, userId, fileName, fileType, storageKey, sizeBytes);
    }

    public Material(UUID id, UUID subjectId, UUID userId, String fileName, String fileType, String storageKey, long sizeBytes) {
        this.id = id;
        this.subjectId = subjectId;
        this.userId = userId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.storageKey = storageKey;
        this.sizeBytes = sizeBytes;
        this.status = MaterialStatus.UPLOADED;
        this.createdAt = Instant.now();
    }

    public boolean isProcessed() {
        return status == MaterialStatus.PROCESSED;
    }

    public void markProcessing() {
        this.status = MaterialStatus.PROCESSING;
        this.errorMessage = null;
    }

    public void markTextExtracted() {
        this.status = MaterialStatus.TEXT_EXTRACTED;
        this.errorMessage = null;
    }

    public void markProcessed() {
        this.status = MaterialStatus.PROCESSED;
        this.errorMessage = null;
        this.processedAt = Instant.now();
    }

    public void markFailed(String errorMessage) {
        this.status = MaterialStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubjectId() {
        return subjectId;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public MaterialStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}
