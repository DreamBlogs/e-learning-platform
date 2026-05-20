package com.example.learning.rag.infrastructure;

import com.example.learning.rag.application.EmbeddingProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@ConditionalOnExpression("!'${app.ai.openai.api-key:}'.isBlank()")
public class OpenAiEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(OpenAiEmbeddingProvider.class);
    private static final int DIMENSIONS = 1536;

    private final RestClient restClient;
    private final String model;

    public OpenAiEmbeddingProvider(
            RestClient.Builder restClientBuilder,
            @Value("${app.ai.openai.api-key}") String apiKey,
            @Value("${app.ai.openai.embedding-model:text-embedding-3-small}") String model
    ) {
        this.model = model;
        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public float[] generateEmbedding(String text) {
        List<float[]> res = generateEmbeddings(List.of(text));
        if (res.isEmpty()) {
            throw new RuntimeException("Empty response from OpenAI embeddings API");
        }
        return res.get(0);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        log.debug("Calling OpenAI embeddings for {} texts using model: {}", texts.size(), model);

        Map<String, Object> request = Map.of(
                "model", model,
                "input", texts
        );

        try {
            Map<String, Object> response = restClient.post()
                    .uri("/embeddings")
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response != null && response.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                List<float[]> embeddings = new ArrayList<>();
                for (Map<String, Object> item : data) {
                    List<Double> vectorNodes = (List<Double>) item.get("embedding");
                    float[] floatVector = new float[vectorNodes.size()];
                    for (int i = 0; i < vectorNodes.size(); i++) {
                        floatVector[i] = vectorNodes.get(i).floatValue();
                    }
                    embeddings.add(floatVector);
                }
                return embeddings;
            }
            throw new RuntimeException("Malformed response from OpenAI embeddings API");
        } catch (Exception e) {
            log.error("Failed to call OpenAI embeddings API: {}", e.getMessage(), e);
            throw new RuntimeException("AI embeddings provider failed", e);
        }
    }

    @Override
    public int getDimensions() {
        return DIMENSIONS;
    }
}
