package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.QuizAttempt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    List<QuizAttempt> findAllByQuizIdAndUserIdOrderByCreatedAtDesc(UUID quizId, UUID userId);
    List<QuizAttempt> findAllByUserIdOrderByCreatedAtDesc(UUID userId);

    @Query("SELECT COUNT(a) FROM QuizAttempt a JOIN Quiz q ON a.quizId = q.id WHERE q.subjectId = :subjectId AND a.userId = :userId")
    long countBySubjectIdAndUserId(@Param("subjectId") UUID subjectId, @Param("userId") UUID userId);
}
