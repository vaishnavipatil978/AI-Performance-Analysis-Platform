package com.platform.report.domain.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetric {

    private UUID id;
    private UUID reportId;
    private String endpoint;
    private String httpMethod;
    private long samples;
    private BigDecimal averageResponseTimeMs;
    private BigDecimal minResponseTimeMs;
    private BigDecimal maxResponseTimeMs;
    private BigDecimal p50ResponseTimeMs;
    private BigDecimal p90ResponseTimeMs;
    private BigDecimal p95ResponseTimeMs;
    private BigDecimal p99ResponseTimeMs;
    private BigDecimal throughput;
    private BigDecimal errorRate;
    private long errorCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Long version;
}
