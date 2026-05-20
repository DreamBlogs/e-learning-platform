package com.example.learning.knowledge.infrastructure;

import com.example.learning.knowledge.domain.Topic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findBySubjectId(UUID subjectId);
    Optional<Topic> findBySubjectIdAndName(UUID subjectId, String name);
}
