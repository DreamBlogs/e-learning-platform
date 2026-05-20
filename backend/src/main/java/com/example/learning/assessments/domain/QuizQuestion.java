package com.example.learning.assessments.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {

    public enum QuestionType {
        MULTIPLE_CHOICE, TRUE_FALSE, SHORT_ANSWER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID quizId;
    private UUID topicId;

    @Transient
    private UUID sourceChunkId;

    @Enumerated(EnumType.STRING)
    private QuestionType questionType;
    private String prompt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options_json", columnDefinition = "jsonb")
    private String optionsJson;
    private String correctAnswer;

    private Instant createdAt = Instant.now();

    protected QuizQuestion() {}

    public QuizQuestion(UUID quizId, UUID topicId, UUID sourceChunkId, QuestionType questionType, String prompt, String optionsJson, String correctAnswer) {
        this.quizId = quizId;
        this.topicId = topicId;
        this.sourceChunkId = sourceChunkId;
        this.questionType = questionType;
        this.prompt = prompt;
        this.optionsJson = optionsJson;
        this.correctAnswer = correctAnswer;
    }

    public UUID getId() { return id; }
    public UUID getQuizId() { return quizId; }
    public UUID getTopicId() { return topicId; }
    public UUID getSourceChunkId() { return sourceChunkId; }
    public QuestionType getQuestionType() { return questionType; }
    public String getPrompt() { return prompt; }
    public String getOptionsJson() { return optionsJson; }
    public String getCorrectAnswer() { return correctAnswer; }
}
