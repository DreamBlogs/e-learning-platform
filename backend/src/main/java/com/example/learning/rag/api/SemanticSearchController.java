package com.example.learning.rag.api;

import com.example.learning.rag.application.SearchResult;
import com.example.learning.rag.application.SemanticSearchService;
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

    public SemanticSearchController(SemanticSearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping
    public ResponseEntity<List<SearchResultResponse>> search(
            @PathVariable UUID subjectId,
            @Valid @RequestBody SearchRequest request
    ) {
        List<SearchResult> results = searchService.search(
                subjectId,
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
