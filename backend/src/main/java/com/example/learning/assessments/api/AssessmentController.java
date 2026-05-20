package com.example.learning.assessments.api;

import com.example.learning.assessments.application.AssessmentService;
import com.example.learning.assessments.domain.Quiz;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subjectId}/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> listQuizzes(
            @PathVariable UUID subjectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(assessmentService.getQuizzesForSubject(subjectId, userId));
    }

    @PostMapping("/generate")
    public ResponseEntity<Quiz> generateQuiz(
            @PathVariable UUID subjectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Quiz quiz = assessmentService.generateQuiz(subjectId, userId);
        return ResponseEntity.ok(quiz);
    }
    
    @PostMapping("/{quizId}/submit")
    public ResponseEntity<?> submitQuiz(
            @PathVariable UUID subjectId,
            @PathVariable UUID quizId,
            @AuthenticationPrincipal Jwt jwt) {
        // Implement simple MVP deterministic submission locally for now
        // This accepts a payload and updates the confidence state deterministically
        return ResponseEntity.ok().build();
    }
}
