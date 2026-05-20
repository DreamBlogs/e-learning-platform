package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.Quiz;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findAllBySubjectIdAndUserIdOrderByCreatedAtDesc(UUID subjectId, UUID userId);
    Optional<Quiz> findByIdAndUserId(UUID id, UUID userId);
    Optional<Quiz> findByIdAndSubjectIdAndUserId(UUID id, UUID subjectId, UUID userId);
    long countBySubjectIdAndUserId(UUID subjectId, UUID userId);
}
