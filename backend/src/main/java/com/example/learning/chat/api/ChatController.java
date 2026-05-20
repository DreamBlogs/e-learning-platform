package com.example.learning.chat.api;

import com.example.learning.chat.application.ChatService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subjectId}/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> chat(
            @PathVariable UUID subjectId,
            @Valid @RequestBody ChatMessageRequest request
    ) {
        String answer = chatService.processChat(subjectId, request.content());
        return ResponseEntity.ok(new ChatResponse(answer));
    }
}
