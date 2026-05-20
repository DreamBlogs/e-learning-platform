package com.example.learning.tasks.application;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record UpdateTaskRequest(
        UUID subjectId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 2000) String description,
        Instant dueDate,
        String priority
) {
}
