package com.example.learning.rag.domain;

import com.example.learning.rag.infrastructure.VectorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "content_chunks")
public class ContentChunk {

    @Id
    private UUID id;

    @Column(name = "material_id", nullable = false)
    private UUID materialId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "text_content", nullable = false, columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "token_count", nullable = false)
    private int tokenCount;

    @Type(VectorType.class)
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private float[] embedding;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ContentChunk() {
    }

    public ContentChunk(UUID materialId, int chunkIndex, String textContent, int tokenCount) {
        this.id = UUID.randomUUID();
        this.materialId = materialId;
        this.chunkIndex = chunkIndex;
        this.textContent = textContent;
        this.tokenCount = tokenCount;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getMaterialId() {
        return materialId;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public String getTextContent() {
        return textContent;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setEmbedding(float[] embedding) {
        if (embedding != null && embedding.length != 1536) {
            throw new IllegalArgumentException("Embedding must be 1536 dimensions");
        }
        this.embedding = embedding;
    }

    public boolean hasEmbedding() {
        return embedding != null;
    }
}
