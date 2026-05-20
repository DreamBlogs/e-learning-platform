package com.example.learning.chat.api;

import com.example.learning.chat.application.ChatResponse;
import com.example.learning.chat.application.ChatService;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subjectId}/chat")
public class ChatController {

    private final ChatService chatService;
    private final CurrentUserProvider currentUserProvider;

    public ChatController(ChatService chatService, CurrentUserProvider currentUserProvider) {
        this.chatService = chatService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ApiResponse<ChatResponse> chat(
            @PathVariable UUID subjectId,
            @RequestBody ChatRequest request
    ) {
        return ApiResponse.ok(chatService.chat(currentUserProvider.get().id(), subjectId, request.message()));
    }
}
