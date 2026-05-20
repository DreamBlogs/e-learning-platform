package com.example.learning.chat.application;

import java.util.List;

public record ChatResponse(
        String message,
        List<String> sources
) {
}
