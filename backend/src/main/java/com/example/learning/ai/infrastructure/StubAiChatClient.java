package com.example.learning.ai.infrastructure;

import com.example.learning.ai.application.AiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnExpression("'${app.ai.openai.api-key:}'.isBlank()")
public class StubAiChatClient implements AiClient {

    @Override
    public String complete(String prompt) {
        return "AI is not configured. Add OPENAI_API_KEY to backend/.env and restart the backend.";
    }
}
