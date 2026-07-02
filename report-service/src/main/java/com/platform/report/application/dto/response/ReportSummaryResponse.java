package com.platform.report.application.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ReportSummaryResponse(
        UUID id,
        String title,
        String tool,
        String status,
        String fileName,
        long fileSize,
        OffsetDateTime createdAt
) {}
