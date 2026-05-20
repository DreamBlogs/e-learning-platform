package com.example.learning.ai.infrastructure;

import com.example.learning.ai.application.AiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("'${app.ai.groq.api-key:}'.isBlank() || '${app.ai.groq.api-key:}' == 'your-openai-api-key-here'")
public class StubAiChatClient implements AiClient {

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return "AI is not configured. Add OPENAI_API_KEY to backend/.env and restart the backend.";
    }

    @Override
    public String chatWithContext(String systemPrompt, String context, String userMessage) {
        return "AI is not configured. Add OPENAI_API_KEY to backend/.env and restart the backend.";
    }
}
