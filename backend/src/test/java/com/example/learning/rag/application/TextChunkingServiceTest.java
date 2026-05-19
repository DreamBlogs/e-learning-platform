package com.example.learning.rag.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextChunkingServiceTest {

    private final TextChunkingService chunkingService = new TextChunkingService();

    @Test
    void shouldChunkTextIntoMultipleChunks() {
        String longText = generateLongText(5000);

        List<TextChunk> chunks = chunkingService.chunkText(longText);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.size()).isGreaterThan(1);
    }

    @Test
    void shouldPreserveChunkOrder() {
        String text = generateLongText(3000);

        List<TextChunk> chunks = chunkingService.chunkText(text);

        for (int i = 0; i < chunks.size(); i++) {
            assertThat(chunks.get(i).index()).isEqualTo(i);
        }
    }

    @Test
    void shouldRespectTokenLimits() {
        String text = generateLongText(5000);

        List<TextChunk> chunks = chunkingService.chunkText(text);

        for (TextChunk chunk : chunks) {
            assertThat(chunk.tokenCount()).isBetween(100, 1500);
        }
    }

    @Test
    void shouldHandleEmptyText() {
        List<TextChunk> chunks = chunkingService.chunkText("");

        assertThat(chunks).isEmpty();
    }

    @Test
    void shouldHandleShortText() {
        String shortText = "This is a short text.";

        List<TextChunk> chunks = chunkingService.chunkText(shortText);

        assertThat(chunks).hasSize(1);
        assertThat(chunks.get(0).text()).isEqualTo(shortText);
    }

    @Test
    void shouldNormalizeWhitespace() {
        String text = "Line 1\r\nLine 2\rLine 3\n\n\n\nLine 4";

        List<TextChunk> chunks = chunkingService.chunkText(text);

        assertThat(chunks).isNotEmpty();
        assertThat(chunks.get(0).text()).doesNotContain("\r");
        assertThat(chunks.get(0).text()).doesNotContain("\n\n\n");
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
