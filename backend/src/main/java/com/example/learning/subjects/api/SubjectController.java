package com.example.learning.subjects.api;

import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import com.example.learning.subjects.application.SubjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/subjects")
public class SubjectController {

    private final SubjectService subjectService;
    private final CurrentUserProvider currentUserProvider;

    public SubjectController(SubjectService subjectService, CurrentUserProvider currentUserProvider) {
        this.subjectService = subjectService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectResponse> create(@Valid @RequestBody CreateSubjectRequest request) {
        return ApiResponse.ok(subjectService.create(currentUserProvider.get().id(), request));
    }

    @GetMapping
    public ApiResponse<List<SubjectResponse>> list() {
        return ApiResponse.ok(subjectService.list(currentUserProvider.get().id()));
    }

    @GetMapping("/{subjectId}")
    public ApiResponse<SubjectResponse> get(@PathVariable UUID subjectId) {
        return ApiResponse.ok(subjectService.get(currentUserProvider.get().id(), subjectId));
    }

    @PatchMapping("/{subjectId}")
    public ApiResponse<SubjectResponse> update(
            @PathVariable UUID subjectId,
            @Valid @RequestBody UpdateSubjectRequest request
    ) {
        return ApiResponse.ok(subjectService.update(currentUserProvider.get().id(), subjectId, request));
    }

    @DeleteMapping("/{subjectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID subjectId) {
        subjectService.delete(currentUserProvider.get().id(), subjectId);
    }
}
