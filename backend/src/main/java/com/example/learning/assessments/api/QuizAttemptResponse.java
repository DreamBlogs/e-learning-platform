package com.example.learning.assessments.api;

import java.time.Instant;
import java.util.UUID;

public record QuizAttemptResponse(
        UUID id,
        UUID quizId,
        Integer score,
        Integer totalQuestions,
        Instant startedAt,
        Instant completedAt
) {
}
