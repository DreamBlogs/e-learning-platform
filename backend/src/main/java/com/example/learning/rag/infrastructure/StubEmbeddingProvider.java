package com.example.learning.rag.infrastructure;

import com.example.learning.rag.application.EmbeddingProvider;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "embedding.provider", havingValue = "stub", matchIfMissing = true)
public class StubEmbeddingProvider implements EmbeddingProvider {

    private static final Logger log = LoggerFactory.getLogger(StubEmbeddingProvider.class);
    private static final int DIMENSIONS = 1536;

    @Override
    public float[] generateEmbedding(String text) {
        log.debug("Generating stub embedding for text of length: {}", text.length());
        return generateDeterministicEmbedding(text);
    }

    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        log.debug("Generating {} stub embeddings", texts.size());
        List<float[]> embeddings = new ArrayList<>(texts.size());
        for (String text : texts) {
            embeddings.add(generateEmbedding(text));
        }
        return embeddings;
    }

    @Override
    public int getDimensions() {
        return DIMENSIONS;
    }

    private float[] generateDeterministicEmbedding(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes());

            float[] embedding = new float[DIMENSIONS];
            for (int i = 0; i < DIMENSIONS; i++) {
                int byteIndex = i % hash.length;
                embedding[i] = (hash[byteIndex] & 0xFF) / 255.0f - 0.5f;
            }

            normalize(embedding);
            return embedding;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private void normalize(float[] vector) {
        double sumSquares = 0.0;
        for (float v : vector) {
            sumSquares += v * v;
        }
        double magnitude = Math.sqrt(sumSquares);
        if (magnitude > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= magnitude;
            }
        }
    }
}
