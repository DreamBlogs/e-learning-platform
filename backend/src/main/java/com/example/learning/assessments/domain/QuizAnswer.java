package com.example.learning.assessments.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_answers")
public class QuizAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID attemptId;
    private UUID questionId;
    private String userAnswer;
    private boolean isCorrect;
    private Instant createdAt = Instant.now();

    protected QuizAnswer() {}

    public QuizAnswer(UUID attemptId, UUID questionId, String userAnswer, boolean isCorrect) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }

    public UUID getId() { return id; }
    public UUID getAttemptId() { return attemptId; }
    public UUID getQuestionId() { return questionId; }
    public String getUserAnswer() { return userAnswer; }
    public boolean isCorrect() { return isCorrect; }
    public Instant getCreatedAt() { return createdAt; }
}
