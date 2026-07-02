package com.platform.analysis.presentation.controller;

import com.platform.common.dto.ApiResponse;
import com.platform.common.dto.PageResponse;
import com.platform.analysis.application.dto.response.AnalysisResponse;
import com.platform.analysis.application.service.AnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/analyses")
@RequiredArgsConstructor
@Tag(name = "Analyses", description = "AI performance analysis results")
@SecurityRequirement(name = "bearerAuth")
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/{id}")
    @Operation(summary = "Get analysis by ID")
    public ApiResponse<AnalysisResponse> getAnalysis(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        return ApiResponse.success("Analysis retrieved",
                analysisService.getAnalysis(id, userId, "ADMIN".equals(role)));
    }

    @GetMapping("/report/{reportId}")
    @Operation(summary = "Get analysis by report ID")
    public ApiResponse<AnalysisResponse> getAnalysisByReport(
            @PathVariable UUID reportId,
            @RequestHeader("X-User-ID") UUID userId,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String role) {
        return ApiResponse.success("Analysis retrieved",
                analysisService.getAnalysisByReportId(reportId, userId, "ADMIN".equals(role)));
    }

    @GetMapping
    @Operation(summary = "List current user's analyses")
    public ApiResponse<PageResponse<AnalysisResponse>> getUserAnalyses(
            @RequestHeader("X-User-ID") UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("Analyses retrieved",
                analysisService.getUserAnalyses(userId, page, size));
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all analyses (Admin only)")
    public ApiResponse<PageResponse<AnalysisResponse>> getAllAnalyses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success("All analyses retrieved",
                analysisService.getAllAnalyses(page, size));
    }
}
