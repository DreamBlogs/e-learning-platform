package com.example.learning.rag.api;

import com.example.learning.common.application.CurrentUser;
import com.example.learning.common.application.CurrentUserProvider;
import com.example.learning.common.exception.ResourceNotFoundException;
import com.example.learning.rag.application.SearchResult;
import com.example.learning.rag.application.SemanticSearchService;
import com.example.learning.subjects.domain.Subject;
import com.example.learning.subjects.infrastructure.SubjectRepository;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects/{subjectId}/search")
public class SemanticSearchController {

    private final SemanticSearchService searchService;
    private final SubjectRepository subjectRepository;
    private final CurrentUserProvider currentUserProvider;

    public SemanticSearchController(
            SemanticSearchService searchService,
            SubjectRepository subjectRepository,
            CurrentUserProvider currentUserProvider
    ) {
        this.searchService = searchService;
        this.subjectRepository = subjectRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    public ResponseEntity<List<SearchResultResponse>> search(
            @PathVariable UUID subjectId,
            @Valid @RequestBody SearchRequest request
    ) {
        CurrentUser user = currentUserProvider.get();
        Subject subject = subjectRepository.findByIdAndUserId(subjectId, user.id())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", subjectId));

        List<SearchResult> results = searchService.search(
                subject.getId(),
                request.query(),
                request.limit()
        );

        List<SearchResultResponse> response = results.stream()
                .map(result -> new SearchResultResponse(
                        result.chunkId(),
                        result.materialId(),
                        result.chunkIndex(),
                        result.textContent(),
                        result.similarityScore()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }
}
