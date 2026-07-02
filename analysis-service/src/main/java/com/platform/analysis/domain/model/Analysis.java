package com.platform.analysis.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Analysis {

    private UUID id;
    private UUID reportId;
    private UUID userId;
    private AnalysisStatus status;
    private Integer promptTokens;
    private Integer completionTokens;
    private String aiModel;
    private List<AnalysisFinding> findings;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String createdBy;
    private Long version;
}
