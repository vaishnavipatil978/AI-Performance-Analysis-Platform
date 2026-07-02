package com.platform.report.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ReportResponse(
        UUID id,
        UUID userId,
        String title,
        String description,
        String tool,
        String status,
        String fileName,
        long fileSize,
        List<ReportMetricResponse> metrics,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
