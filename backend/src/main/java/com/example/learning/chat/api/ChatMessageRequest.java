package com.example.learning.chat.api;

import jakarta.validation.constraints.NotBlank;

public record ChatMessageRequest(
        @NotBlank(message = "Message content must not be empty") String content
) {}
