package com.example.learning.rag.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.rag.domain.ContentChunk;
import com.example.learning.rag.infrastructure.ContentChunkRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SemanticSearchServiceIntegrationTest {

    @Autowired
    private SemanticSearchService searchService;

    @Autowired
    private VectorStorageService vectorStorageService;

    @Autowired
    private ContentChunkRepository chunkRepository;

    private UUID subjectId;
    private UUID materialId;

    @BeforeEach
    void setUp() {
        subjectId = UUID.randomUUID();
        materialId = UUID.randomUUID();
    }

    @Test
    void shouldReturnRelevantChunks() {
        String text = """
                Machine learning is a subset of artificial intelligence.
                Deep learning uses neural networks with multiple layers.
                Natural language processing helps computers understand human language.
                Computer vision enables machines to interpret visual information.
                """;

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<SearchResult> results = searchService.search(subjectId, "neural networks", 5);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.similarityScore() >= 0 && r.similarityScore() <= 1);
    }

    @Test
    void shouldRespectLimit() {
        String text = generateLongText(5000);

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<SearchResult> results = searchService.search(subjectId, "sample text", 3);

        assertThat(results).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void shouldReturnEmptyForEmptyQuery() {
        String text = "Some content for testing.";

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<SearchResult> results = searchService.search(subjectId, "", 10);

        assertThat(results).isEmpty();
    }

    @Test
    void shouldIncludeMaterialIdInResults() {
        String text = "Test content for material identification.";

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<SearchResult> results = searchService.search(subjectId, "test content", 5);

        assertThat(results).isNotEmpty();
        assertThat(results).allMatch(r -> r.materialId().equals(materialId));
    }

    @Test
    void shouldOrderBySimilarityScore() {
        String text = generateLongText(3000);

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<SearchResult> results = searchService.search(subjectId, "sample sentence", 10);

        if (results.size() > 1) {
            for (int i = 0; i < results.size() - 1; i++) {
                assertThat(results.get(i).similarityScore())
                        .isGreaterThanOrEqualTo(results.get(i + 1).similarityScore());
            }
        }
    }

    private String generateLongText(int length) {
        StringBuilder sb = new StringBuilder();
        String sentence = "This is a sample sentence for testing purposes. ";
        while (sb.length() < length) {
            sb.append(sentence);
        }
        return sb.toString();
    }
}
