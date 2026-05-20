package com.example.learning.materials.api;

import com.example.learning.materials.domain.MaterialExtractedText;
import java.time.Instant;
import java.util.UUID;

public record MaterialExtractedTextResponse(
        UUID materialId,
        String textContent,
        Integer pageCount,
        long characterCount,
        Instant createdAt,
        Instant updatedAt
) {
    public static MaterialExtractedTextResponse from(MaterialExtractedText extractedText) {
        return new MaterialExtractedTextResponse(
                extractedText.getMaterialId(),
                extractedText.getTextContent(),
                extractedText.getPageCount(),
                extractedText.getCharacterCount(),
                extractedText.getCreatedAt(),
                extractedText.getUpdatedAt()
        );
    }
}
