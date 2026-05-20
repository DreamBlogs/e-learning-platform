package com.example.learning.assessments.api;

import com.example.learning.assessments.application.QuizService;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUserProvider currentUserProvider;

    public QuizController(QuizService quizService, CurrentUserProvider currentUserProvider) {
        this.quizService = quizService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/subjects/{subjectId}/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuizResponse> create(
            @PathVariable UUID subjectId,
            @Valid @RequestBody CreateQuizRequest request
    ) {
        return ApiResponse.ok(quizService.create(currentUserProvider.get().id(), subjectId, request));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuizQuestionResponse> addQuestion(
            @PathVariable UUID quizId,
            @Valid @RequestBody CreateQuizQuestionRequest request
    ) {
        return ApiResponse.ok(quizService.addQuestion(currentUserProvider.get().id(), quizId, request));
    }

    @GetMapping("/subjects/{subjectId}/quizzes")
    public ApiResponse<List<QuizResponse>> list(@PathVariable UUID subjectId) {
        return ApiResponse.ok(quizService.listBySubject(currentUserProvider.get().id(), subjectId));
    }

    @GetMapping("/quizzes/{quizId}")
    public ApiResponse<QuizResponse> get(@PathVariable UUID quizId) {
        return ApiResponse.ok(quizService.get(currentUserProvider.get().id(), quizId));
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public ApiResponse<List<QuizQuestionResponse>> getQuestions(@PathVariable UUID quizId) {
        return ApiResponse.ok(quizService.getQuestions(currentUserProvider.get().id(), quizId));
    }

    @PostMapping("/quizzes/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<QuizAttemptResponse> submitAttempt(
            @Valid @RequestBody SubmitQuizAttemptRequest request
    ) {
        return ApiResponse.ok(quizService.submitAttempt(currentUserProvider.get().id(), request));
    }

    @GetMapping("/quizzes/{quizId}/attempts")
    public ApiResponse<List<QuizAttemptResponse>> getAttempts(@PathVariable UUID quizId) {
        return ApiResponse.ok(quizService.getAttempts(currentUserProvider.get().id(), quizId));
    }
}
