package com.example.learning.materials.application;

import java.util.UUID;

public record MaterialProcessingRequestedEvent(
        UUID materialId
) {
}
