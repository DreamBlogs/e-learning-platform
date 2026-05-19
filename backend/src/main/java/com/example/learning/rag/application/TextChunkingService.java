package com.example.learning.rag.application;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TextChunkingService {

    private static final int MIN_CHUNK_SIZE = 500;
    private static final int MAX_CHUNK_SIZE = 1000;
    private static final int OVERLAP_SIZE = 100;
    private static final double CHARS_PER_TOKEN = 4.0;

    public List<TextChunk> chunkText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<TextChunk> chunks = new ArrayList<>();
        String normalizedText = normalizeText(text);

        int targetChunkChars = (int) (MAX_CHUNK_SIZE * CHARS_PER_TOKEN);
        int overlapChars = (int) (OVERLAP_SIZE * CHARS_PER_TOKEN);

        int start = 0;
        int chunkIndex = 0;

        while (start < normalizedText.length()) {
            int end = Math.min(start + targetChunkChars, normalizedText.length());

            if (end < normalizedText.length()) {
                end = findSentenceBoundary(normalizedText, end);
            }

            String chunkText = normalizedText.substring(start, end).trim();

            if (!chunkText.isEmpty()) {
                int tokenCount = estimateTokenCount(chunkText);
                chunks.add(new TextChunk(chunkIndex++, chunkText, tokenCount));
            }

            start = end - overlapChars;
            if (start >= normalizedText.length()) {
                break;
            }
        }

        return chunks;
    }

    private String normalizeText(String text) {
        return text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]+", " ")
                .trim();
    }

    private int findSentenceBoundary(String text, int position) {
        int searchStart = Math.max(0, position - 200);
        int searchEnd = Math.min(text.length(), position + 200);

        String searchWindow = text.substring(searchStart, searchEnd);
        int relativePos = position - searchStart;

        int sentenceEnd = findNearestSentenceEnd(searchWindow, relativePos);
        if (sentenceEnd != -1) {
            return searchStart + sentenceEnd;
        }

        int paragraphEnd = searchWindow.lastIndexOf("\n\n", relativePos);
        if (paragraphEnd != -1 && paragraphEnd > relativePos - 100) {
            return searchStart + paragraphEnd + 2;
        }

        int newlineEnd = searchWindow.lastIndexOf("\n", relativePos);
        if (newlineEnd != -1 && newlineEnd > relativePos - 100) {
            return searchStart + newlineEnd + 1;
        }

        return position;
    }

    private int findNearestSentenceEnd(String text, int position) {
        for (int i = position; i < Math.min(position + 100, text.length()); i++) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                if (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    return i + 1;
                }
            }
        }

        for (int i = position; i >= Math.max(0, position - 100); i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?') {
                if (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
                    return i + 1;
                }
            }
        }

        return -1;
    }

    private int estimateTokenCount(String text) {
        return (int) Math.ceil(text.length() / CHARS_PER_TOKEN);
    }
}
