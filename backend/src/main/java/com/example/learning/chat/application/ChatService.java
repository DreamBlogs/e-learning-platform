package com.example.learning.chat.application;

import com.example.learning.ai.application.AiClient;
import com.example.learning.rag.application.SearchResult;
import com.example.learning.rag.application.SemanticSearchService;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    
    private final SemanticSearchService semanticSearchService;
    private final AiClient aiClient;

    public ChatService(SemanticSearchService semanticSearchService, AiClient aiClient) {
        this.semanticSearchService = semanticSearchService;
        this.aiClient = aiClient;
    }

    public String processChat(UUID subjectId, String userMessage) {
        log.info("Processing chat for subject {}: {}", subjectId, userMessage);
        
        // 1. Retrieve relevant chunks based on user query
        List<SearchResult> searchResults = semanticSearchService.search(subjectId, userMessage, 5);
        
        // 2. Assemble context prompt
        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < searchResults.size(); i++) {
            contextBuilder.append("--- Document ").append(i + 1).append(" ---\n");
            contextBuilder.append(searchResults.get(i).textContent()).append("\n\n");
        }
        
        String context = contextBuilder.toString();
        
        String prompt = assemblePrompt(context, userMessage);
        
        // 3. Query LLM
        return aiClient.complete(prompt);
    }
    
    private String assemblePrompt(String context, String userMessage) {
        return """
            You are a helpful study assistant. Use the provided context from the user's uploaded materials to answer the question.
            If the context does not contain the answer, say "I couldn't find the answer in the uploaded materials" and try to provide a general helpful answer if possible, but clarify that it is not from the materials.
            
            CONTEXT:
            """ + context + """
            
            USER QUESTION:
            """ + userMessage + """
            
            ANSWER:
            """;
    }
}
