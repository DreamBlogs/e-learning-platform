package com.example.learning.materials.api;

import com.example.learning.common.api.ApiResponse;
import com.example.learning.common.application.CurrentUserProvider;
import com.example.learning.materials.application.MaterialService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/api")
public class MaterialController {

    private final MaterialService materialService;
    private final CurrentUserProvider currentUserProvider;

    public MaterialController(MaterialService materialService, CurrentUserProvider currentUserProvider) {
        this.materialService = materialService;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping("/subjects/{subjectId}/materials")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaterialResponse> create(
            @PathVariable UUID subjectId,
            @Valid @RequestBody CreateMaterialRequest request
    ) {
        return ApiResponse.ok(materialService.create(currentUserProvider.get().id(), subjectId, request));
    }

    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    @PostMapping("/subjects/{subjectId}/materials/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MaterialResponse> upload(
            @PathVariable UUID subjectId,
            @RequestParam("file") MultipartFile file
    ) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 50MB");
        }
        return ApiResponse.ok(materialService.upload(currentUserProvider.get().id(), subjectId, file));
    }

    @GetMapping("/subjects/{subjectId}/materials")
    public ApiResponse<List<MaterialResponse>> list(@PathVariable UUID subjectId) {
        return ApiResponse.ok(materialService.list(currentUserProvider.get().id(), subjectId));
    }

    @GetMapping("/materials/{materialId}")
    public ApiResponse<MaterialResponse> get(@PathVariable UUID materialId) {
        return ApiResponse.ok(materialService.get(currentUserProvider.get().id(), materialId));
    }

    @GetMapping("/materials/{materialId}/extracted-text")
    public ApiResponse<MaterialExtractedTextResponse> getExtractedText(@PathVariable UUID materialId) {
        return ApiResponse.ok(materialService.getExtractedText(currentUserProvider.get().id(), materialId));
    }

    @DeleteMapping("/materials/{materialId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID materialId) {
        materialService.delete(currentUserProvider.get().id(), materialId);
    }

    @GetMapping("/materials/{materialId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(@PathVariable UUID materialId) {
        return materialService.download(currentUserProvider.get().id(), materialId);
    }
}
