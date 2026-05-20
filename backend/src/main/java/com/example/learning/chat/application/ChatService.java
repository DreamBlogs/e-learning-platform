package com.example.learning.chat.application;

import com.example.learning.ai.application.AiClient;
import com.example.learning.rag.application.SemanticSearchService;
import com.example.learning.rag.application.SearchResult;
import com.example.learning.subjects.domain.Subject;
import com.example.learning.subjects.infrastructure.SubjectRepository;
import com.example.learning.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final AiClient aiClient;
    private final SemanticSearchService searchService;
    private final SubjectRepository subjectRepository;

    public ChatService(
            AiClient aiClient,
            SemanticSearchService searchService,
            SubjectRepository subjectRepository
    ) {
        this.aiClient = aiClient;
        this.searchService = searchService;
        this.subjectRepository = subjectRepository;
    }

    public ChatResponse chat(UUID userId, UUID subjectId, String message) {
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        List<SearchResult> context = searchService.search(subjectId, message, 3);

        String contextText = context.isEmpty()
                ? "No relevant materials found."
                : context.stream()
                        .map(r -> "- " + r.textContent())
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("");

        String systemPrompt = """
                You are an AI tutor for the subject "%s".
                Answer questions based on the provided context from learning materials.
                If the context doesn't contain relevant information, say so and provide a general answer.
                Be concise and educational. Use markdown formatting for code examples.
                """.formatted(subject.getName());

        String response = aiClient.chatWithContext(systemPrompt, contextText, message);

        List<String> sources = context.stream()
                .map(r -> "Chunk from material " + r.materialId())
                .toList();

        return new ChatResponse(response, sources);
    }
}
