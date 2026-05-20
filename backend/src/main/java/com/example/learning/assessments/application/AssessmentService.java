package com.example.learning.assessments.application;

import com.example.learning.ai.application.AiClient;
import com.example.learning.assessments.domain.Quiz;
import com.example.learning.assessments.domain.QuizQuestion;
import com.example.learning.assessments.domain.QuizAttempt;
import com.example.learning.assessments.domain.QuizAnswer;

import com.example.learning.assessments.infrastructure.QuizQuestionRepository;
import com.example.learning.assessments.infrastructure.QuizRepository;
import com.example.learning.assessments.infrastructure.QuizAttemptRepository;
import com.example.learning.assessments.infrastructure.QuizAnswerRepository;
import com.example.learning.knowledge.infrastructure.KnowledgeStateRepository;
import com.example.learning.rag.application.SearchResult;
import com.example.learning.rag.application.SemanticSearchService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AssessmentService {

    private final QuizRepository quizRepository;
    private final QuizQuestionRepository questionRepository;
    private final AiClient aiClient;
    private final SemanticSearchService searchService;
    private final ObjectMapper objectMapper;
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final KnowledgeStateRepository knowledgeStateRepository;

    public AssessmentService(
            QuizRepository quizRepository,
            QuizQuestionRepository questionRepository,
            AiClient aiClient,
            SemanticSearchService searchService,
            ObjectMapper objectMapper,
            QuizAttemptRepository quizAttemptRepository,
            QuizAnswerRepository quizAnswerRepository,
            KnowledgeStateRepository knowledgeStateRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.aiClient = aiClient;
        this.searchService = searchService;
        this.objectMapper = objectMapper;
        this.quizAttemptRepository = quizAttemptRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.knowledgeStateRepository = knowledgeStateRepository;
    }

    public List<Quiz> getQuizzesForSubject(UUID subjectId, UUID userId) {
        return quizRepository.findBySubjectIdAndUserIdOrderByCreatedAtDesc(subjectId, userId);
    }

    public Quiz generateQuiz(UUID subjectId, UUID userId) {
        // 1. Create Quiz Record
        Quiz quiz = new Quiz(subjectId, userId, "AI Assessment");
        quizRepository.save(quiz);

        try {
            // 2. Fetch context chunks (mocking broad query for MVP)
            List<SearchResult> chunks = searchService.search(subjectId, "core academic concepts and definitions", 10);
            
            StringBuilder context = new StringBuilder();
            for (SearchResult chunk : chunks) {
                context.append(chunk.textContent()).append("\n\n");
            }

            if (context.toString().isBlank()) {
                quiz.markReady();
                quizRepository.save(quiz);
                return quiz;
            }

            // 3. Prompt LLM for structured JSON
            String prompt = """
                You are an expert teacher. Generate a 3-question multiple choice quiz based strictly on the provided context.
                Output ONLY a JSON array of objects with the following schema:
                [
                  {
                    "prompt": "String question text",
                    "options": ["Option A", "Option B", "Option C"],
                    "correctAnswer": "Exact string of correct option"
                  }
                ]
                
                CONTEXT:
                """ + context.toString();

            String jsonResponse = aiClient.complete(prompt);

            // Strip out markdown code blocks if the LLM adds them
            if (jsonResponse.startsWith("```json")) {
                jsonResponse = jsonResponse.substring(7);
                if (jsonResponse.endsWith("```")) {
                    jsonResponse = jsonResponse.substring(0, jsonResponse.length() - 3);
                }
            }

            // 4. Parse and Save Questions
            JsonNode root = objectMapper.readTree(jsonResponse.trim());
            if (root.isArray()) {
                for (JsonNode node : root) {
                    List<String> options = new ArrayList<>();
                    node.get("options").forEach(opt -> options.add(opt.asText()));

                    QuizQuestion question = new QuizQuestion(
                            quiz.getId(),
                            null, // Not linking topic for MVP V1 yet
                            null, // sourceChunkId not used in MVP
                            QuizQuestion.QuestionType.MULTIPLE_CHOICE,
                            node.get("prompt").asText(),
                            objectMapper.writeValueAsString(options),
                            node.get("correctAnswer").asText()
                    );
                    questionRepository.save(question);
                }
            }
            
            quiz.markReady();
            quizRepository.save(quiz);

        } catch (Exception e) {
            e.printStackTrace();
            // Leave in GENERATING/FAILED state
        }

        return quiz;
    }

    // New method to handle quiz submission
    public QuizAttempt submitQuiz(UUID quizId, UUID userId, List<AnswerSubmission> answers) {
        // Load quiz and ensure it belongs to user
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        if (!quiz.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Quiz does not belong to user");
        }

        // Load all questions for the quiz
        List<QuizQuestion> questions = questionRepository.findByQuizId(quizId);
        int total = questions.size();
        int correctCount = 0;

        QuizAttempt attempt = new QuizAttempt(quizId, userId);
        quizAttemptRepository.save(attempt);

        for (QuizQuestion q : questions) {
            // Find submitted answer
            String submitted = answers.stream()
                    .filter(a -> a.getQuestionId().equals(q.getId()))
                    .map(AnswerSubmission::getAnswer)
                    .findFirst()
                    .orElse("");

            boolean isCorrect;
            if (q.getQuestionType() == QuizQuestion.QuestionType.MULTIPLE_CHOICE) {
                isCorrect = q.getCorrectAnswer().equalsIgnoreCase(submitted.trim());
            } else {
                // SHORT_ANSWER – use lightweight AI validation via AiClient (semantic similarity)
                // For MVP we fallback to case‑insensitive containment check if AI fails
                try {
                    String validationPrompt = """
                        Determine if the student's answer correctly addresses the question.
                        Return ONLY 'true' or 'false'.\n                        Question: %s\n                        Correct answer: %s\n                        Student answer: %s\n                    """.formatted(q.getPrompt(), q.getCorrectAnswer(), submitted);
                    String aiResult = aiClient.complete(validationPrompt).trim().toLowerCase();
                    isCorrect = aiResult.contains("true");
                } catch (Exception e) {
                    isCorrect = q.getCorrectAnswer().equalsIgnoreCase(submitted.trim());
                }
            }

            if (isCorrect) {
                correctCount++;
            }

            // Persist answer record
            QuizAnswer answer = new QuizAnswer(attempt.getId(), q.getId(), submitted, isCorrect);
            quizAnswerRepository.save(answer);

            // Update knowledge state if topic linked
            if (q.getTopicId() != null) {
                final boolean wasCorrect = isCorrect;
                final int currentCorrect = correctCount;
                knowledgeStateRepository.findByUserIdAndSubjectIdAndTopicId(userId, quiz.getSubjectId(), q.getTopicId())
                        .ifPresent(state -> state.applyQuizResult(wasCorrect, (currentCorrect * 100) / total));
            }
        }

        int score = (int) Math.round((double) correctCount / total * 100);
        attempt.setScore(score);
        quizAttemptRepository.save(attempt);
        return attempt;
    }
}
