package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.QuizAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findAllByQuizIdAndUserIdOrderByCreatedAtDesc(UUID quizId, UUID userId);
    List<QuizAttempt> findAllByUserIdOrderByCreatedAtDesc(UUID userId);
    long countBySubjectIdAndUserId(UUID subjectId, UUID userId);
}
