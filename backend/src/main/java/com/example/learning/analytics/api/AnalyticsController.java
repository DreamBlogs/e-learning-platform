package com.example.learning.analytics.api;

import com.example.learning.analytics.application.AnalyticsOverview;
import com.example.learning.analytics.application.AnalyticsService;
import com.example.learning.analytics.application.SubjectAnalytics;
import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import com.example.learning.knowledge.application.TopicMasteryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final CurrentUserProvider currentUserProvider;

    public AnalyticsController(AnalyticsService analyticsService, CurrentUserProvider currentUserProvider) {
        this.analyticsService = analyticsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/overview")
    public ApiResponse<AnalyticsOverview> overview() {
        return ApiResponse.ok(analyticsService.getOverview(currentUserProvider.get().id()));
    }

    @GetMapping("/subjects")
    public ApiResponse<List<SubjectAnalytics>> subjects() {
        return ApiResponse.ok(analyticsService.getSubjectAnalytics(currentUserProvider.get().id()));
    }

    @GetMapping("/weakest-topics")
    public ApiResponse<List<TopicMasteryResponse>> weakestTopics(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.ok(analyticsService.getWeakestTopics(currentUserProvider.get().id(), limit));
    }

    @GetMapping("/strongest-topics")
    public ApiResponse<List<TopicMasteryResponse>> strongestTopics(
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ApiResponse.ok(analyticsService.getStrongestTopics(currentUserProvider.get().id(), limit));
    }
}
