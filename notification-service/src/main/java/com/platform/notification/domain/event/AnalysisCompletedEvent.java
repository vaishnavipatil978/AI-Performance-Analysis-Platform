package com.platform.notification.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AnalysisCompletedEvent(
        UUID analysisId,
        UUID reportId,
        UUID userId,
        int findingsCount,
        String status,
        String correlationId,
        OffsetDateTime occurredAt
) {}
