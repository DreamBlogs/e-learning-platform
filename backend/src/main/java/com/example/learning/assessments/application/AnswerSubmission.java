package com.example.learning.assessments.application;

import java.util.UUID;

public class AnswerSubmission {
    private UUID questionId;
    private String answer;

    public AnswerSubmission() {}

    public AnswerSubmission(UUID questionId, String answer) {
        this.questionId = questionId;
        this.answer = answer;
    }

    public UUID getQuestionId() { return questionId; }
    public String getAnswer() { return answer; }
    public void setQuestionId(UUID questionId) { this.questionId = questionId; }
    public void setAnswer(String answer) { this.answer = answer; }
}
