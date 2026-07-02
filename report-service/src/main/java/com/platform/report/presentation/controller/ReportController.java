package com.platform.report.presentation.controller;

import com.platform.common.dto.ApiResponse;
import com.platform.common.dto.PageResponse;
import com.platform.report.application.dto.response.ReportResponse;
import com.platform.report.application.dto.response.ReportSummaryResponse;
import com.platform.report.application.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Performance report upload and management")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a performance report")
    public ApiResponse<ReportResponse> uploadReport(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam String tool,
            @RequestPart("file") MultipartFile file) {
        return ApiResponse.success("Report uploaded", reportService.uploadReport(userId, title, description, tool, file));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get report by ID")
    public ApiResponse<ReportResponse> getReport(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        return ApiResponse.success("Report retrieved", reportService.getReport(id, userId, "ADMIN".equals(role)));
    }

    @GetMapping
    @Operation(summary = "List current user's reports")
    public ApiResponse<PageResponse<ReportSummaryResponse>> getUserReports(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Reports retrieved", reportService.getUserReports(userId, page, size));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all reports (Admin only)")
    public ApiResponse<PageResponse<ReportSummaryResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("All reports retrieved", reportService.getAllReports(page, size));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a report")
    public void deleteReport(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        reportService.deleteReport(id, userId, "ADMIN".equals(role));
    }

    @PostMapping("/{id}/analyse")
    @Operation(summary = "Request AI analysis for a report")
    public ApiResponse<Void> requestAnalysis(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId) {
        reportService.requestAnalysis(id, userId);
        return ApiResponse.success("Analysis requested", null);
    }
}
