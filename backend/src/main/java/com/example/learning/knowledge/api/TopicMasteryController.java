package com.example.learning.knowledge.api;

import com.example.learning.knowledge.application.TopicMasteryResponse;
import com.example.learning.knowledge.application.TopicMasteryService;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TopicMasteryController {

    private final TopicMasteryService masteryService;
    private final CurrentUserProvider currentUserProvider;

    public TopicMasteryController(TopicMasteryService masteryService, CurrentUserProvider currentUserProvider) {
        this.masteryService = masteryService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/subjects/{subjectId}/topics")
    public ApiResponse<List<TopicMasteryResponse>> getBySubject(@PathVariable UUID subjectId) {
        return ApiResponse.ok(masteryService.getBySubject(currentUserProvider.get().id(), subjectId));
    }
}
