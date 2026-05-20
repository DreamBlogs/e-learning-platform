package com.example.learning.knowledge.application;

import java.time.Instant;
import java.util.UUID;

public record TopicMasteryResponse(
        UUID id,
        UUID subjectId,
        String topic,
        Integer confidence,
        Integer questionsAttempted,
        Integer correctAnswers,
        Instant lastTested
) {
}
