package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.QuizAnswer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {
    List<QuizAnswer> findAllByAttemptId(UUID attemptId);
}
