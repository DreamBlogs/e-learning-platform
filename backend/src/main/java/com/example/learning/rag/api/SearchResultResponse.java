package com.example.learning.rag.api;

import java.util.UUID;

public record SearchResultResponse(
        UUID chunkId,
        UUID materialId,
        int chunkIndex,
        String textContent,
        double similarityScore
) {
}
