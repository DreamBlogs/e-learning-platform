package com.example.learning.ai.application;

public interface AiClient {
    String chat(String systemPrompt, String userMessage);
    String chatWithContext(String systemPrompt, String context, String userMessage);
}
