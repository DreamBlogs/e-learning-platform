package com.example.learning.knowledge.application;

import com.example.learning.knowledge.api.KnowledgeStateDTO;
import com.example.learning.knowledge.domain.KnowledgeState;
import com.example.learning.knowledge.domain.Topic;
import com.example.learning.knowledge.infrastructure.KnowledgeStateRepository;
import com.example.learning.knowledge.infrastructure.TopicRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeService {

    private final KnowledgeStateRepository knowledgeStateRepository;
    private final TopicRepository topicRepository;

    public KnowledgeService(KnowledgeStateRepository knowledgeStateRepository, TopicRepository topicRepository) {
        this.knowledgeStateRepository = knowledgeStateRepository;
        this.topicRepository = topicRepository;
    }

    public List<KnowledgeStateDTO> getKnowledgeStates(UUID subjectId, UUID userId) {
        List<KnowledgeState> states = knowledgeStateRepository.findByUserIdAndSubjectId(userId, subjectId);
        List<Topic> topics = topicRepository.findBySubjectId(subjectId);
        
        return states.stream().map(state -> {
            String topicName = topics.stream()
                .filter(t -> t.getId().equals(state.getTopicId()))
                .map(Topic::getName)
                .findFirst()
                .orElse("Unknown Topic");
                
            return new KnowledgeStateDTO(
                state.getId(),
                state.getTopicId(),
                topicName,
                state.getConfidenceScore(),
                state.getMistakeCount(),
                state.getLastQuizScore(),
                state.getUpdatedAt()
            );
        }).collect(Collectors.toList());
    }
}
