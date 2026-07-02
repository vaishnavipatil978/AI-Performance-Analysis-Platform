package com.platform.report.infrastructure.persistence.entity;

import com.platform.common.audit.AuditableEntity;
import com.platform.report.domain.model.PerformanceTool;
import com.platform.report.domain.model.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PerformanceTool tool;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, length = 1000)
    private String filePath;

    @Column(nullable = false)
    private long fileSize;

    @OneToMany(mappedBy = "report", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ReportMetricEntity> metrics;
}
