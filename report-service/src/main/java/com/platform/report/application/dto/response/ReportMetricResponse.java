package com.platform.report.application.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ReportMetricResponse(
        UUID id,
        String endpoint,
        String httpMethod,
        long samples,
        BigDecimal averageResponseTimeMs,
        BigDecimal minResponseTimeMs,
        BigDecimal maxResponseTimeMs,
        BigDecimal p50ResponseTimeMs,
        BigDecimal p90ResponseTimeMs,
        BigDecimal p95ResponseTimeMs,
        BigDecimal p99ResponseTimeMs,
        BigDecimal throughput,
        BigDecimal errorRate,
        long errorCount
) {}
