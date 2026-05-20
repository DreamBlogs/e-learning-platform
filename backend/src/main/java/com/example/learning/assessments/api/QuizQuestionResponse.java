package com.example.learning.assessments.api;

import java.util.UUID;

public record QuizQuestionResponse(
        UUID id,
        UUID quizId,
        String questionText,
        String optionA,
        String optionB,
        String optionC,
        String optionD,
        String explanation,
        String topic,
        String difficulty,
        Integer position
) {
}
