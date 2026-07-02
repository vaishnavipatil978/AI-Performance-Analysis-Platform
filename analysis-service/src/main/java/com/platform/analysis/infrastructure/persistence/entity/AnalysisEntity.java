package com.platform.analysis.infrastructure.persistence.entity;

import com.platform.common.audit.AuditableEntity;
import com.platform.analysis.domain.model.AnalysisStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "analyses")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID reportId;

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    private Integer promptTokens;
    private Integer completionTokens;
    private String aiModel;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AnalysisFindingEntity> findings;
}
