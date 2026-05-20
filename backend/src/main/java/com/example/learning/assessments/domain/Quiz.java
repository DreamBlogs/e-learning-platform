package com.example.learning.assessments.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID subjectId;
    private UUID userId;
    
    private String title;
    private String status;
    
    private Instant createdAt = Instant.now();

    protected Quiz() {}

    public Quiz(UUID subjectId, UUID userId, String title) {
        this.subjectId = subjectId;
        this.userId = userId;
        this.title = title;
        this.status = "GENERATING";
    }

    public void markReady() {
        this.status = "READY";
    }

    public UUID getId() { return id; }
    public UUID getSubjectId() { return subjectId; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
