package com.platform.analysis.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisFinding {

    private UUID id;
    private UUID analysisId;
    private FindingSeverity severity;
    private String category;
    private String title;
    private String description;
    private String recommendation;
    private String affectedEndpoint;
    private String metricValue;
    private String thresholdValue;
    private OffsetDateTime createdAt;
}
