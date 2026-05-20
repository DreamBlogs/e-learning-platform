package com.example.learning.ai.infrastructure;

import com.example.learning.ai.application.AiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroqAiClient implements AiClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-8b-instant";

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String apiKey;

    public GroqAiClient(@Value("${app.ai.groq.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank() || apiKey.equals("your-openai-api-key-here")) {
            return "AI is not configured. Please set app.ai.groq.api-key in your environment.";
        }

        try {
            Map<String, Object> payload = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userMessage)
                    ),
                    "temperature", 0.3,
                    "max_tokens", 1024
            );

            String json = mapper.writeValueAsString(payload);
            RequestBody body = RequestBody.create(json, JSON);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    return "AI service error: " + response.code();
                }
                String responseBody = response.body().string();
                JsonNode root = mapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (IOException e) {
            return "AI service unavailable: " + e.getMessage();
        }
    }

    @Override
    public String chatWithContext(String systemPrompt, String context, String userMessage) {
        String fullSystemPrompt = systemPrompt + "\n\nRelevant context from materials:\n" + context;
        return chat(fullSystemPrompt, userMessage);
    }
}
