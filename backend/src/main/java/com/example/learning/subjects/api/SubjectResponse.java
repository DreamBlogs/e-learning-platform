package com.example.learning.subjects.api;

import com.example.learning.subjects.domain.Subject;
import java.time.Instant;
import java.util.UUID;

public record SubjectResponse(
        UUID id,
        String name,
        String description,
        String color,
        Instant createdAt,
        Instant updatedAt
) {
    public static SubjectResponse from(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getColor(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
