package com.example.learning.analytics.application;

import com.example.learning.knowledge.application.TopicMasteryResponse;

public record AnalyticsOverview(
        Integer avgConfidence,
        Integer topicsMastered,
        Integer totalTopics,
        TopicMasteryResponse weakestArea,
        Integer studyStreak,
        Long quizzesCompleted
) {
}
