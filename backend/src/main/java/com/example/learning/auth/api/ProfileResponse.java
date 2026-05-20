package com.example.learning.auth.api;

import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String email,
        String displayName
) {
}
