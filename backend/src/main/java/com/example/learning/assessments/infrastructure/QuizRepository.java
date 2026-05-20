package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.Quiz;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    List<Quiz> findBySubjectIdAndUserIdOrderByCreatedAtDesc(UUID subjectId, UUID userId);
}
