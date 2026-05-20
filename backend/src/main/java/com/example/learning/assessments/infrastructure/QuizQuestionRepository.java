package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.QuizQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, UUID> {
    List<QuizQuestion> findAllByQuizIdOrderByPosition(UUID quizId);
    void deleteAllByQuizId(UUID quizId);
}
