package com.example.learning.knowledge.infrastructure;

import com.example.learning.knowledge.domain.KnowledgeState;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnowledgeStateRepository extends JpaRepository<KnowledgeState, UUID> {
    List<KnowledgeState> findByUserIdAndSubjectId(UUID userId, UUID subjectId);
    Optional<KnowledgeState> findByUserIdAndSubjectIdAndTopicId(UUID userId, UUID subjectId, UUID topicId);
}
