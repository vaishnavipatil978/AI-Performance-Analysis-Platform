package com.platform.report.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    private UUID id;
    private UUID userId;
    private String title;
    private String description;
    private PerformanceTool tool;
    private ReportStatus status;
    private String fileName;
    private String filePath;
    private long fileSize;
    private List<ReportMetric> metrics;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private Long version;
}
