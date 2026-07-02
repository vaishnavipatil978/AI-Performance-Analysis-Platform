package com.platform.report.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportUploadedEvent(
        UUID reportId,
        UUID userId,
        String tool,
        String filePath,
        String correlationId,
        OffsetDateTime occurredAt
) {}
