package com.example.learning.rag.application;

import java.util.UUID;

public record SearchResult(
        UUID chunkId,
        UUID materialId,
        int chunkIndex,
        String textContent,
        double similarityScore
) {
}
