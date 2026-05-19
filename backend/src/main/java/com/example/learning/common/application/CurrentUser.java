package com.example.learning.common.application;

import java.util.UUID;

public record CurrentUser(
        UUID id,
        String email
) {
}
