package com.example.learning.knowledge.domain;

import com.example.learning.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "topic_mastery")
public class TopicMastery extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(nullable = false, length = 255)
    private String topic;

    @Column(nullable = false)
    private Integer confidence;

    @Column(name = "questions_attempted", nullable = false)
    private Integer questionsAttempted;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(name = "last_tested")
    private Instant lastTested;

    protected TopicMastery() {
    }

    public TopicMastery(UUID userId, UUID subjectId, String topic) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.subjectId = subjectId;
        this.topic = topic;
        this.confidence = 0;
        this.questionsAttempted = 0;
        this.correctAnswers = 0;
    }

    public void updateFromQuizResult(boolean correct) {
        this.questionsAttempted++;
        if (correct) this.correctAnswers++;
        this.lastTested = Instant.now();
        recalculateConfidence();
    }

    private void recalculateConfidence() {
        if (questionsAttempted == 0) {
            this.confidence = 0;
        } else {
            double accuracy = (double) correctAnswers / questionsAttempted;
            double volumeFactor = Math.min(1.0, questionsAttempted / 20.0);
            this.confidence = (int) Math.round(accuracy * volumeFactor * 100);
        }
    }

    public UUID getId() { return id; }
    public UUID getUserId() { return userId; }
    public UUID getSubjectId() { return subjectId; }
    public String getTopic() { return topic; }
    public Integer getConfidence() { return confidence; }
    public Integer getQuestionsAttempted() { return questionsAttempted; }
    public Integer getCorrectAnswers() { return correctAnswers; }
    public Instant getLastTested() { return lastTested; }
}
