package com.example.learning.rag.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SearchRequest(
        @NotBlank(message = "Query is required")
        String query,

        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 50, message = "Limit must not exceed 50")
        Integer limit
) {
    public SearchRequest {
        if (limit == null) {
            limit = 10;
        }
    }
}
