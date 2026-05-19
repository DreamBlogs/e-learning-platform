package com.example.learning.rag.application;

import com.example.learning.rag.domain.ContentChunk;
import com.example.learning.rag.infrastructure.ContentChunkRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VectorStorageService {

    private static final Logger log = LoggerFactory.getLogger(VectorStorageService.class);

    private final ContentChunkRepository chunkRepository;
    private final TextChunkingService chunkingService;
    private final EmbeddingProvider embeddingProvider;

    public VectorStorageService(
            ContentChunkRepository chunkRepository,
            TextChunkingService chunkingService,
            EmbeddingProvider embeddingProvider
    ) {
        this.chunkRepository = chunkRepository;
        this.chunkingService = chunkingService;
        this.embeddingProvider = embeddingProvider;
    }

    @Transactional
    public void processAndStoreChunks(UUID materialId, String extractedText) {
        log.info("Processing text for material. materialId={}, textLength={}", materialId, extractedText.length());

        if (chunkRepository.existsByMaterialId(materialId)) {
            log.info("Chunks already exist for material, deleting old chunks. materialId={}", materialId);
            chunkRepository.deleteByMaterialId(materialId);
        }

        List<TextChunk> textChunks = chunkingService.chunkText(extractedText);
        log.info("Text chunked. materialId={}, chunkCount={}", materialId, textChunks.size());

        if (textChunks.isEmpty()) {
            log.warn("No chunks generated for material. materialId={}", materialId);
            return;
        }

        List<ContentChunk> contentChunks = new ArrayList<>();
        for (TextChunk textChunk : textChunks) {
            ContentChunk chunk = new ContentChunk(
                    materialId,
                    textChunk.index(),
                    textChunk.text(),
                    textChunk.tokenCount()
            );
            contentChunks.add(chunk);
        }

        chunkRepository.saveAll(contentChunks);
        log.info("Chunks saved without embeddings. materialId={}, count={}", materialId, contentChunks.size());

        generateAndStoreEmbeddings(contentChunks);
    }

    @Transactional
    public void generateAndStoreEmbeddings(List<ContentChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        UUID materialId = chunks.get(0).getMaterialId();
        log.info("Generating embeddings. materialId={}, chunkCount={}", materialId, chunks.size());

        try {
            List<String> texts = chunks.stream()
                    .map(ContentChunk::getTextContent)
                    .toList();

            List<float[]> embeddings = embeddingProvider.generateEmbeddings(texts);

            for (int i = 0; i < chunks.size(); i++) {
                chunks.get(i).setEmbedding(embeddings.get(i));
            }

            chunkRepository.saveAll(chunks);
            log.info("Embeddings generated and stored. materialId={}, count={}", materialId, chunks.size());

        } catch (Exception e) {
            log.error("Failed to generate embeddings. materialId={}, error={}", materialId, e.getMessage(), e);
            throw new RuntimeException("Failed to generate embeddings for material: " + materialId, e);
        }
    }

    @Transactional
    public void retryEmbeddingGeneration(UUID materialId) {
        log.info("Retrying embedding generation. materialId={}", materialId);

        List<ContentChunk> chunks = chunkRepository.findByMaterialIdOrderByChunkIndex(materialId);

        if (chunks.isEmpty()) {
            log.warn("No chunks found for material. materialId={}", materialId);
            return;
        }

        List<ContentChunk> chunksWithoutEmbeddings = chunks.stream()
                .filter(chunk -> !chunk.hasEmbedding())
                .toList();

        if (chunksWithoutEmbeddings.isEmpty()) {
            log.info("All chunks already have embeddings. materialId={}", materialId);
            return;
        }

        log.info("Found chunks without embeddings. materialId={}, count={}",
                materialId, chunksWithoutEmbeddings.size());

        generateAndStoreEmbeddings(chunksWithoutEmbeddings);
    }

    public long getChunkCount(UUID materialId) {
        return chunkRepository.countByMaterialId(materialId);
    }

    public List<ContentChunk> getChunks(UUID materialId) {
        return chunkRepository.findByMaterialIdOrderByChunkIndex(materialId);
    }
}
