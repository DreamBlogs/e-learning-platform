package com.example.learning.assessments.infrastructure;

import com.example.learning.assessments.domain.QuizAnswer;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, UUID> {
}
