package com.example.learning.assessments.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuizResponse(
        UUID id,
        UUID subjectId,
        String title,
        String description,
        Integer timeLimitMinutes,
        Integer questionCount,
        Instant createdAt
) {
}
