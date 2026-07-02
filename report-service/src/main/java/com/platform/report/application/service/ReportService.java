package com.platform.report.application.service;

import com.platform.common.dto.PageResponse;
import com.platform.report.application.dto.response.ReportResponse;
import com.platform.report.application.dto.response.ReportSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ReportService {

    ReportResponse uploadReport(UUID userId, String title, String description, String tool, MultipartFile file);

    ReportResponse getReport(UUID reportId, UUID userId, boolean isAdmin);

    PageResponse<ReportSummaryResponse> getUserReports(UUID userId, int page, int size);

    PageResponse<ReportSummaryResponse> getAllReports(int page, int size);

    void deleteReport(UUID reportId, UUID userId, boolean isAdmin);

    void requestAnalysis(UUID reportId, UUID userId);
}
