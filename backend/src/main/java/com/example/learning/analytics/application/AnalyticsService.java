package com.example.learning.analytics.application;

import com.example.learning.assessments.infrastructure.QuizAttemptRepository;
import com.example.learning.knowledge.application.TopicMasteryResponse;
import com.example.learning.knowledge.application.TopicMasteryService;
import com.example.learning.subjects.domain.Subject;
import com.example.learning.subjects.infrastructure.SubjectRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final QuizAttemptRepository attemptRepository;
    private final TopicMasteryService topicMasteryService;
    private final SubjectRepository subjectRepository;

    public AnalyticsService(
            QuizAttemptRepository attemptRepository,
            TopicMasteryService topicMasteryService,
            SubjectRepository subjectRepository
    ) {
        this.attemptRepository = attemptRepository;
        this.topicMasteryService = topicMasteryService;
        this.subjectRepository = subjectRepository;
    }

    public AnalyticsOverview getOverview(UUID userId) {
        List<TopicMasteryResponse> allTopics = topicMasteryService.getBySubject(userId, null);
        List<TopicMasteryResponse> weakest = topicMasteryService.getWeakest(userId, 4);
        List<TopicMasteryResponse> strongest = topicMasteryService.getStrongest(userId, 4);

        int masteredTopics = (int) allTopics.stream()
                .filter(t -> t.confidence() >= 80)
                .count();

        double avgConfidence = allTopics.isEmpty() ? 0 :
                allTopics.stream().mapToInt(TopicMasteryResponse::confidence).average().orElse(0);

        TopicMasteryResponse weakestTopic = weakest.isEmpty() ? null : weakest.get(0);

        int studyStreak = calculateStreak(userId);
        long quizCount = attemptRepository.findAllByUserIdOrderByCreatedAtDesc(userId).size();

        return new AnalyticsOverview(
                (int) Math.round(avgConfidence),
                masteredTopics,
                allTopics.size(),
                weakestTopic,
                studyStreak,
                quizCount
        );
    }

    public List<SubjectAnalytics> getSubjectAnalytics(UUID userId) {
        List<Subject> subjects = subjectRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return subjects.stream()
                .map(s -> {
                    List<TopicMasteryResponse> topics = topicMasteryService.getBySubject(userId, s.getId());
                    long quizCount = attemptRepository.countBySubjectIdAndUserId(s.getId(), userId);
                    double avgConfidence = topics.isEmpty() ? 0 :
                            topics.stream().mapToInt(TopicMasteryResponse::confidence).average().orElse(0);
                    return new SubjectAnalytics(
                            s.getId(),
                            s.getName(),
                            s.getCode(),
                            s.getColor(),
                            (int) Math.round(avgConfidence),
                            topics.size(),
                            quizCount
                    );
                })
                .toList();
    }

    public List<TopicMasteryResponse> getWeakestTopics(UUID userId, int limit) {
        return topicMasteryService.getWeakest(userId, limit);
    }

    public List<TopicMasteryResponse> getStrongestTopics(UUID userId, int limit) {
        return topicMasteryService.getStrongest(userId, limit);
    }

    private int calculateStreak(UUID userId) {
        List<com.example.learning.assessments.domain.QuizAttempt> attempts =
                attemptRepository.findAllByUserIdOrderByCreatedAtDesc(userId);

        if (attempts.isEmpty()) return 0;

        int streak = 1;
        Instant lastDate = attempts.get(0).getCompletedAt() != null
                ? attempts.get(0).getCompletedAt()
                : attempts.get(0).getStartedAt();

        for (int i = 1; i < attempts.size(); i++) {
            Instant currentDate = attempts.get(i).getCompletedAt() != null
                    ? attempts.get(i).getCompletedAt()
                    : attempts.get(i).getStartedAt();

            long daysBetween = ChronoUnit.DAYS.between(currentDate, lastDate);
            if (daysBetween == 1) {
                streak++;
                lastDate = currentDate;
            } else if (daysBetween > 1) {
                break;
            }
        }

        return streak;
    }
}
