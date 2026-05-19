package com.example.learning.rag.application;

import com.example.learning.rag.infrastructure.ContentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SemanticSearchService {

    private static final Logger log = LoggerFactory.getLogger(SemanticSearchService.class);

    private final ContentChunkRepository chunkRepository;
    private final EmbeddingProvider embeddingProvider;

    public SemanticSearchService(
            ContentChunkRepository chunkRepository,
            EmbeddingProvider embeddingProvider
    ) {
        this.chunkRepository = chunkRepository;
        this.embeddingProvider = embeddingProvider;
    }

    @Transactional(readOnly = true)
    public List<SearchResult> search(UUID subjectId, String query, int limit) {
        log.info("Performing semantic search. subjectId={}, query={}, limit={}", subjectId, query, limit);

        if (query == null || query.isBlank()) {
            log.warn("Empty query provided for search. subjectId={}", subjectId);
            return List.of();
        }

        float[] queryEmbedding = embeddingProvider.generateEmbedding(query);
        String embeddingString = formatEmbeddingForPostgres(queryEmbedding);

        List<Object[]> results = chunkRepository.findSimilarChunksBySubject(subjectId, embeddingString, limit);

        List<SearchResult> searchResults = new ArrayList<>();
        for (Object[] row : results) {
            UUID chunkId = UUID.fromString(row[0].toString());
            UUID materialId = UUID.fromString(row[1].toString());
            int chunkIndex = ((Number) row[2]).intValue();
            String textContent = (String) row[3];
            double similarity = ((Number) row[11]).doubleValue();

            searchResults.add(new SearchResult(chunkId, materialId, chunkIndex, textContent, similarity));
        }

        log.info("Search completed. subjectId={}, resultsCount={}", subjectId, searchResults.size());
        return searchResults;
    }

    private String formatEmbeddingForPostgres(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
