package com.platform.notification.application.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String type,
        String title,
        String message,
        UUID referenceId,
        String referenceType,
        boolean read,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {}
