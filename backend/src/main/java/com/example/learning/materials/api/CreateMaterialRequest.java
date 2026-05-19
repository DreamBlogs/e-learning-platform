package com.example.learning.materials.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateMaterialRequest(
        @NotBlank @Size(max = 512) String fileName,
        @NotBlank @Size(max = 120) String fileType,
        @NotBlank @Size(max = 1024) String storageKey,
        @Min(1) long sizeBytes
) {
}
