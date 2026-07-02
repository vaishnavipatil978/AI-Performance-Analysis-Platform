package com.platform.user.application.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String role,
        boolean active,
        OffsetDateTime createdAt
) {
}
