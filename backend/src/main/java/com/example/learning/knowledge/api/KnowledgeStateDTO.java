package com.example.learning.knowledge.api;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeStateDTO(
    UUID id,
    UUID topicId,
    String topicName, // We map the name over so UI easily renders it
    double confidenceScore,
    int mistakeCount,
    Integer lastQuizScore,
    Instant updatedAt
) {}
