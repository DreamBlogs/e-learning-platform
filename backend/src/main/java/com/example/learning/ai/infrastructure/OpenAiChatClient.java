package com.example.learning.ai.infrastructure;

import com.example.learning.ai.application.AiClient;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OpenAiChatClient implements AiClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);
    
    private final RestClient restClient;
    private final String model;

    public OpenAiChatClient(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4o-mini}") String model
    ) {
        this.model = model;
        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public String complete(String prompt) {
        if (prompt == null || prompt.isBlank()) return "";
        
        log.info("Calling OpenAI chat completion with model: {}", model);
        
        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.3
        );

        try {
             Map<String, Object> response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(Map.class);
             
             if (response != null && response.containsKey("choices")) {
                 List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                 if (!choices.isEmpty()) {
                     Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                     return (String) message.get("content");
                 }
             }
             return "No response generated.";
        } catch (Exception e) {
            log.error("Failed to call OpenAI chat API: {}", e.getMessage(), e);
            throw new RuntimeException("AI provider failed to complete request", e);
        }
    }
}
