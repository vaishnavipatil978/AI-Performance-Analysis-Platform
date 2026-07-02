package com.platform.analysis.application.dto.response;

import java.util.UUID;

public record FindingResponse(
        UUID id,
        String severity,
        String category,
        String title,
        String description,
        String recommendation,
        String affectedEndpoint,
        String metricValue,
        String thresholdValue
) {}
