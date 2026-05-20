package com.example.learning.knowledge.application;

import com.example.learning.knowledge.domain.TopicMastery;
import com.example.learning.knowledge.infrastructure.TopicMasteryRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TopicMasteryService {

    private final TopicMasteryRepository repository;

    public TopicMasteryService(TopicMasteryRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void recordAnswer(UUID userId, UUID subjectId, String topic, boolean correct) {
        TopicMastery mastery = repository.findByUserIdAndSubjectIdAndTopic(userId, subjectId, topic)
                .orElseGet(() -> {
                    TopicMastery newMastery = new TopicMastery(userId, subjectId, topic);
                    return repository.save(newMastery);
                });
        mastery.updateFromQuizResult(correct);
        repository.save(mastery);
    }

    public List<TopicMasteryResponse> getBySubject(UUID userId, UUID subjectId) {
        List<TopicMastery> topics = subjectId == null
                ? repository.findAllByUserIdOrderByTopic(userId)
                : repository.findAllByUserIdAndSubjectIdOrderByTopic(userId, subjectId);
        return topics.stream().map(this::toResponse).toList();
    }

    public List<TopicMasteryResponse> getWeakest(UUID userId, int limit) {
        return repository.findAllByUserIdOrderByConfidenceAsc(userId).stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    public List<TopicMasteryResponse> getStrongest(UUID userId, int limit) {
        return repository.findAllByUserIdOrderByConfidenceDesc(userId).stream()
                .limit(limit)
                .map(this::toResponse)
                .toList();
    }

    private TopicMasteryResponse toResponse(TopicMastery m) {
        return new TopicMasteryResponse(
                m.getId(),
                m.getSubjectId(),
                m.getTopic(),
                m.getConfidence(),
                m.getQuestionsAttempted(),
                m.getCorrectAnswers(),
                m.getLastTested()
        );
    }
}
