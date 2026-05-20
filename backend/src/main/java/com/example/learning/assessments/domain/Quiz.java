package com.example.learning.assessments.domain;

import com.example.learning.common.domain.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
public class Quiz extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "subject_id", nullable = false)
    private UUID subjectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    protected Quiz() {
    }

    public Quiz(UUID userId, UUID subjectId, String title, String description, Integer timeLimitMinutes) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.subjectId = subjectId;
        this.title = title;
        this.description = description;
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public UUID getId() { return id; }
    public UUID getSubjectId() { return subjectId; }
    public UUID getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getTimeLimitMinutes() { return timeLimitMinutes; }
}
