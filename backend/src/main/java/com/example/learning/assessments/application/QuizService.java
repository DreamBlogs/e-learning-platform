package com.example.learning.assessments.application;

import com.example.learning.assessments.api.CreateQuizQuestionRequest;
import com.example.learning.assessments.api.CreateQuizRequest;
import com.example.learning.assessments.api.QuizAttemptResponse;
import com.example.learning.assessments.api.QuizQuestionResponse;
import com.example.learning.assessments.api.QuizResponse;
import com.example.learning.assessments.api.SubmitQuizAttemptRequest;
import com.example.learning.assessments.domain.Quiz;
import com.example.learning.assessments.domain.QuizAnswer;
import com.example.learning.assessments.domain.QuizAttempt;
import com.example.learning.assessments.domain.QuizQuestion;
import com.example.learning.assessments.infrastructure.QuizAnswerRepository;
import com.example.learning.assessments.infrastructure.QuizAttemptRepository;
import com.example.learning.assessments.infrastructure.QuizQuestionRepository;
import com.example.learning.assessments.infrastructure.QuizRepository;
import com.example.learning.common.exception.ResourceNotFoundException;
import com.example.learning.knowledge.application.TopicMasteryService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuizAnswerRepository answerRepository;
    private final TopicMasteryService topicMasteryService;

    public QuizService(
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            QuizAttemptRepository attemptRepository,
            QuizAnswerRepository answerRepository,
            TopicMasteryService topicMasteryService
    ) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.answerRepository = answerRepository;
        this.topicMasteryService = topicMasteryService;
    }

    @Transactional
    public QuizResponse create(UUID userId, UUID subjectId, CreateQuizRequest request) {
        Quiz quiz = new Quiz(userId, subjectId, request.title(), request.description(), request.timeLimitMinutes());
        return toResponse(quizRepository.save(quiz), 0);
    }

    @Transactional
    public QuizQuestionResponse addQuestion(UUID userId, UUID quizId, CreateQuizQuestionRequest request) {
        Quiz quiz = quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));

        QuizQuestion question = new QuizQuestion(
                quiz.getId(),
                request.questionText(),
                request.optionA(),
                request.optionB(),
                request.optionC(),
                request.optionD(),
                request.correctOption().toUpperCase(),
                request.explanation(),
                request.topic(),
                QuizQuestion.Difficulty.valueOf(request.difficulty()),
                request.position()
        );
        return toQuestionResponse(questionRepository.save(question));
    }

    public List<QuizResponse> listBySubject(UUID userId, UUID subjectId) {
        List<Quiz> quizzes = quizRepository.findAllBySubjectIdAndUserIdOrderByCreatedAtDesc(subjectId, userId);
        return quizzes.stream()
                .map(q -> toResponse(q, questionRepository.findAllByQuizIdOrderByPosition(q.getId()).size()))
                .toList();
    }

    public QuizResponse get(UUID userId, UUID quizId) {
        Quiz quiz = quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));
        int questionCount = questionRepository.findAllByQuizIdOrderByPosition(quiz.getId()).size();
        return toResponse(quiz, questionCount);
    }

    public List<QuizQuestionResponse> getQuestions(UUID userId, UUID quizId) {
        quizRepository.findByIdAndUserId(quizId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", quizId));
        return questionRepository.findAllByQuizIdOrderByPosition(quizId).stream()
                .map(this::toQuestionResponse)
                .toList();
    }

    @Transactional
    public QuizAttemptResponse submitAttempt(UUID userId, SubmitQuizAttemptRequest request) {
        Quiz quiz = quizRepository.findByIdAndUserId(request.quizId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz", request.quizId()));

        List<QuizQuestion> questions = questionRepository.findAllByQuizIdOrderByPosition(quiz.getId());
        QuizAttempt attempt = new QuizAttempt(userId, quiz.getId(), questions.size());
        attemptRepository.save(attempt);

        int score = 0;
        for (SubmitQuizAttemptRequest.AnswerSubmission submission : request.answers()) {
            QuizQuestion question = questions.stream()
                    .filter(q -> q.getId().equals(submission.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Question", submission.questionId()));

            boolean isCorrect = question.getCorrectOption().equalsIgnoreCase(submission.selectedOption());
            if (isCorrect) score++;

            answerRepository.save(new QuizAnswer(
                    attempt.getId(),
                    question.getId(),
                    submission.selectedOption().toUpperCase(),
                    isCorrect
            ));

            if (question.getTopic() != null && !question.getTopic().isBlank()) {
                topicMasteryService.recordAnswer(userId, quiz.getSubjectId(), question.getTopic(), isCorrect);
            }
        }

        attempt.complete(score);
        return toAttemptResponse(attempt);
    }

    public List<QuizAttemptResponse> getAttempts(UUID userId, UUID quizId) {
        return attemptRepository.findAllByQuizIdAndUserIdOrderByCreatedAtDesc(quizId, userId).stream()
                .map(this::toAttemptResponse)
                .toList();
    }

    public long countBySubject(UUID userId, UUID subjectId) {
        return attemptRepository.countBySubjectIdAndUserId(subjectId, userId);
    }

    private QuizResponse toResponse(Quiz quiz, int questionCount) {
        return new QuizResponse(
                quiz.getId(),
                quiz.getSubjectId(),
                quiz.getTitle(),
                quiz.getDescription(),
                quiz.getTimeLimitMinutes(),
                questionCount,
                quiz.getCreatedAt()
        );
    }

    private QuizQuestionResponse toQuestionResponse(QuizQuestion q) {
        return new QuizQuestionResponse(
                q.getId(),
                q.getQuizId(),
                q.getQuestionText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getExplanation(),
                q.getTopic(),
                q.getDifficulty().name(),
                q.getPosition()
        );
    }

    private QuizAttemptResponse toAttemptResponse(QuizAttempt a) {
        return new QuizAttemptResponse(
                a.getId(),
                a.getQuizId(),
                a.getScore(),
                a.getTotalQuestions(),
                a.getStartedAt(),
                a.getCompletedAt()
        );
    }
}
