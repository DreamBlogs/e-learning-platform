package com.example.learning.assessments.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateQuizQuestionRequest(
        @NotBlank @Size(max = 2000) String questionText,
        @NotBlank String optionA,
        @NotBlank String optionB,
        @NotBlank String optionC,
        @NotBlank String optionD,
        @NotBlank String correctOption,
        @Size(max = 1000) String explanation,
        @Size(max = 255) String topic,
        @NotNull String difficulty,
        @NotNull Integer position
) {
}
