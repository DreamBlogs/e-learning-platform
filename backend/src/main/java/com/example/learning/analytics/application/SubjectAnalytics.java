package com.example.learning.analytics.application;

import java.util.UUID;

public record SubjectAnalytics(
        UUID id,
        String name,
        String code,
        String color,
        Integer avgConfidence,
        Integer topicCount,
        Long quizCount
) {
}
