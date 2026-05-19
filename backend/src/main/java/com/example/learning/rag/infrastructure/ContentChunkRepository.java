package com.example.learning.rag.infrastructure;

import com.example.learning.rag.domain.ContentChunk;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentChunkRepository extends JpaRepository<ContentChunk, UUID> {

    List<ContentChunk> findByMaterialIdOrderByChunkIndex(UUID materialId);

    long countByMaterialId(UUID materialId);

    boolean existsByMaterialId(UUID materialId);

    void deleteByMaterialId(UUID materialId);

    @Query(value = """
        SELECT c.*, 1 - (c.embedding <=> CAST(:queryEmbedding AS vector)) AS similarity
        FROM content_chunks c
        JOIN materials m ON c.material_id = m.id
        WHERE m.subject_id = :subjectId
          AND c.embedding IS NOT NULL
        ORDER BY c.embedding <=> CAST(:queryEmbedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findSimilarChunksBySubject(
            @Param("subjectId") UUID subjectId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );
}
