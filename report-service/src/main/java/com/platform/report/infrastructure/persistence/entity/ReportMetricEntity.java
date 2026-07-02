package com.platform.report.infrastructure.persistence.entity;

import com.platform.common.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "report_metrics")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetricEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ReportEntity report;

    @Column(nullable = false, length = 500)
    private String endpoint;

    @Column(length = 10)
    private String httpMethod;

    @Column(nullable = false)
    private long samples;

    @Column(precision = 12, scale = 2)
    private BigDecimal averageResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal minResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal p50ResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal p90ResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal p95ResponseTimeMs;

    @Column(precision = 12, scale = 2)
    private BigDecimal p99ResponseTimeMs;

    @Column(precision = 12, scale = 4)
    private BigDecimal throughput;

    @Column(precision = 6, scale = 4)
    private BigDecimal errorRate;

    @Column(nullable = false)
    private long errorCount;
}
