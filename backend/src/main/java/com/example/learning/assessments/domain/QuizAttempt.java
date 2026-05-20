package com.example.learning.assessments.domain;

import com.example.learning.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "quiz_id", nullable = false)
    private UUID quizId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected QuizAttempt() {
    }

    public QuizAttempt(UUID userId, UUID quizId, Integer totalQuestions) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.quizId = quizId;
        this.totalQuestions = totalQuestions;
        this.score = 0;
        this.startedAt = Instant.now();
    }

    public void complete(Integer score) {
        this.score = score;
        this.completedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getQuizId() { return quizId; }
    public UUID getUserId() { return userId; }
    public Integer getScore() { return score; }
    public Integer getTotalQuestions() { return totalQuestions; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getCompletedAt() { return completedAt; }
}
