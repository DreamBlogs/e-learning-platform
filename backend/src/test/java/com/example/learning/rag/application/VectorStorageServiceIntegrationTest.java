package com.example.learning.rag.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.learning.rag.domain.ContentChunk;
import com.example.learning.rag.infrastructure.ContentChunkRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VectorStorageServiceIntegrationTest {

    @Autowired
    private VectorStorageService vectorStorageService;

    @Autowired
    private ContentChunkRepository chunkRepository;

    @Autowired
    private EmbeddingProvider embeddingProvider;

    @Test
    void shouldProcessAndStoreChunks() {
        UUID materialId = UUID.randomUUID();
        String text = generateSampleText(3000);

        vectorStorageService.processAndStoreChunks(materialId, text);

        long chunkCount = vectorStorageService.getChunkCount(materialId);
        assertThat(chunkCount).isGreaterThan(0);

        List<ContentChunk> chunks = vectorStorageService.getChunks(materialId);
        assertThat(chunks).isNotEmpty();
        assertThat(chunks).allMatch(ContentChunk::hasEmbedding);
    }

    @Test
    void shouldGenerateEmbeddingsWithCorrectDimensions() {
        UUID materialId = UUID.randomUUID();
        String text = "This is a test document for embedding generation.";

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<ContentChunk> chunks = vectorStorageService.getChunks(materialId);
        assertThat(chunks).isNotEmpty();

        for (ContentChunk chunk : chunks) {
            assertThat(chunk.getEmbedding()).hasSize(embeddingProvider.getDimensions());
        }
    }

    @Test
    void shouldPreserveChunkOrder() {
        UUID materialId = UUID.randomUUID();
        String text = generateSampleText(2000);

        vectorStorageService.processAndStoreChunks(materialId, text);

        List<ContentChunk> chunks = vectorStorageService.getChunks(materialId);

        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).getChunkIndex()).isEqualTo(i);
        }
    }

    @Test
    void shouldReplaceExistingChunks() {
        UUID materialId = UUID.randomUUID();
        String text1 = "First version of the text.";
        String text2 = generateSampleText(2000);

        vectorStorageService.processAndStoreChunks(materialId, text1);
        long firstCount = vectorStorageService.getChunkCount(materialId);

        vectorStorageService.processAndStoreChunks(materialId, text2);
        long secondCount = vectorStorageService.getChunkCount(materialId);

        assertThat(secondCount).isGreaterThan(firstCount);
    }

    @Test
    void shouldRetryEmbeddingGeneration() {
        UUID materialId = UUID.randomUUID();
        String text = "Sample text for retry test.";

        ContentChunk chunk = new ContentChunk(materialId, 0, text, 10);
        chunkRepository.save(chunk);

        assertThat(chunk.hasEmbedding()).isFalse();

        vectorStorageService.retryEmbeddingGeneration(materialId);

        ContentChunk updated = chunkRepository.findById(chunk.getId()).orElseThrow();
        assertThat(updated.hasEmbedding()).isTrue();
    }

    private String generateSampleText(int length) {
        StringBuilder sb = new StringBuilder();
        String sentence = "This is a sample sentence for testing the RAG pipeline. ";
        while (sb.length() < length) {
            sb.append(sentence);
        }
        return sb.toString();
    }
}
