package com.example.learning.auth.api;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String displayName,
        String accessToken
) {
}
