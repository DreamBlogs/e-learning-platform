package com.example.learning.auth.api;

import java.util.UUID;

public record CurrentUserResponse(
        UUID id,
        String email
) {
}
