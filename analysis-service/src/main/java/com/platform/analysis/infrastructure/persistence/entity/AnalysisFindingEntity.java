package com.platform.analysis.infrastructure.persistence.entity;

import com.platform.analysis.domain.model.FindingSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis_findings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AnalysisFindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AnalysisEntity analysis;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String recommendation;

    @Column(length = 500)
    private String affectedEndpoint;

    @Column(length = 200)
    private String metricValue;

    @Column(length = 200)
    private String thresholdValue;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
