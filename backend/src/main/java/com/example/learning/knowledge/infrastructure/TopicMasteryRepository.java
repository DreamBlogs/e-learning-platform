package com.example.learning.knowledge.infrastructure;

import com.example.learning.knowledge.domain.TopicMastery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicMasteryRepository extends JpaRepository<TopicMastery, UUID> {
    List<TopicMastery> findAllByUserIdAndSubjectIdOrderByTopic(UUID userId, UUID subjectId);
    List<TopicMastery> findAllByUserIdOrderByTopic(UUID userId);
    Optional<TopicMastery> findByUserIdAndSubjectIdAndTopic(UUID userId, UUID subjectId, String topic);
    List<TopicMastery> findAllByUserIdOrderByConfidenceAsc(UUID userId);
    List<TopicMastery> findAllByUserIdOrderByConfidenceDesc(UUID userId);
}
