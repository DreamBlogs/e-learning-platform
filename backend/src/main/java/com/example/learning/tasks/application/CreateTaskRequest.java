package com.example.learning.tasks.application;

import java.time.Instant;
import java.util.UUID;

public record CreateTaskRequest(
        UUID subjectId,
        String title,
        String description,
        Instant dueDate,
        String priority
) {
}
