package com.platform.analysis.application.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record AnalysisResponse(
        UUID id,
        UUID reportId,
        UUID userId,
        String status,
        String aiModel,
        Integer promptTokens,
        Integer completionTokens,
        List<FindingResponse> findings,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
