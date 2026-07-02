package com.platform.analysis.application.service;

import com.platform.common.dto.PageResponse;
import com.platform.analysis.application.dto.response.AnalysisResponse;
import com.platform.analysis.domain.event.ReportUploadedEvent;

import java.util.UUID;

public interface AnalysisService {

    void processReportUploadedEvent(ReportUploadedEvent event);

    AnalysisResponse getAnalysis(UUID analysisId, UUID userId, boolean isAdmin);

    AnalysisResponse getAnalysisByReportId(UUID reportId, UUID userId, boolean isAdmin);

    PageResponse<AnalysisResponse> getUserAnalyses(UUID userId, int page, int size);

    PageResponse<AnalysisResponse> getAllAnalyses(int page, int size);
}
