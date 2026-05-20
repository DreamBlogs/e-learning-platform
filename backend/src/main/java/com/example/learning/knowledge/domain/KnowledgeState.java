package com.example.learning.knowledge.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "knowledge_states")
public class KnowledgeState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;
    private UUID subjectId;
    private UUID topicId;

    private double confidenceScore = 0.0;
    private int mistakeCount = 0;
    private Integer lastQuizScore;

    private Instant updatedAt = Instant.now();
    private Instant lastReviewedAt = Instant.now();

    protected KnowledgeState() {}

    public KnowledgeState(UUID userId, UUID subjectId, UUID topicId) {
        this.userId = userId;
        this.subjectId = subjectId;
        this.topicId = topicId;
    }

    public void applyQuizResult(boolean isCorrect, int currentQuizScore) {
        if (isCorrect) {
            this.confidenceScore = Math.min(1.0, this.confidenceScore + 0.2);
        } else {
            this.confidenceScore = Math.max(0.0, this.confidenceScore - 0.15);
            this.mistakeCount++;
        }
        this.lastQuizScore = currentQuizScore;
        this.updatedAt = Instant.now();
        this.lastReviewedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getSubjectId() { return subjectId; }
    public UUID getTopicId() { return topicId; }
    public double getConfidenceScore() { return confidenceScore; }
    public int getMistakeCount() { return mistakeCount; }
    public Integer getLastQuizScore() { return lastQuizScore; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Instant getLastReviewedAt() { return lastReviewedAt; }
}
