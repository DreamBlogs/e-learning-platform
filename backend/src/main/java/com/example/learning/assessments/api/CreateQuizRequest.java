package com.example.learning.assessments.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateQuizRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        Integer timeLimitMinutes
) {
}
