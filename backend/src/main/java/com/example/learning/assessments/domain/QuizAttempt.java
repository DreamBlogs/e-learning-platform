package com.example.learning.assessments.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID quizId;
    private UUID userId;
    private Integer score; // percentage 0-100
    private Instant createdAt = Instant.now();

    protected QuizAttempt() {}

    public QuizAttempt(UUID quizId, UUID userId) {
        this.quizId = quizId;
        this.userId = userId;
    }

    public UUID getId() { return id; }
    public UUID getQuizId() { return quizId; }
    public UUID getUserId() { return userId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public Instant getCreatedAt() { return createdAt; }
}
