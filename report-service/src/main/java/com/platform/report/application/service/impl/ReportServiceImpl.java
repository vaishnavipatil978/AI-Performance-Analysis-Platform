package com.platform.report.application.service.impl;

import com.platform.common.dto.PageResponse;
import com.platform.common.exception.BusinessException;
import com.platform.common.util.CorrelationIdUtil;
import com.platform.report.application.dto.response.ReportResponse;
import com.platform.report.application.dto.response.ReportSummaryResponse;
import com.platform.report.application.mapper.ReportMapper;
import com.platform.report.application.service.ReportService;
import com.platform.report.domain.event.ReportUploadedEvent;
import com.platform.report.domain.exception.ReportNotFoundException;
import com.platform.report.domain.model.PerformanceTool;
import com.platform.report.domain.model.Report;
import com.platform.report.domain.model.ReportStatus;
import com.platform.report.domain.repository.ReportRepository;
import com.platform.report.infrastructure.messaging.ReportEventPublisher;
import com.platform.report.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ReportMapper reportMapper;
    private final FileStorageService fileStorageService;
    private final ReportEventPublisher eventPublisher;

    @Override
    @Transactional
    public ReportResponse uploadReport(UUID userId, String title, String description,
                                       String tool, MultipartFile file) {
        PerformanceTool performanceTool = parseToolOrThrow(tool);
        String filePath = fileStorageService.store(file, userId);
        Report report = Report.builder()
                .userId(userId)
                .title(title)
                .description(description)
                .tool(performanceTool)
                .status(ReportStatus.UPLOADED)
                .fileName(file.getOriginalFilename())
                .filePath(filePath)
                .fileSize(file.getSize())
                .build();
        Report saved = reportRepository.save(report);
        eventPublisher.publishReportUploaded(new ReportUploadedEvent(
                saved.getId(), userId, tool, filePath,
                CorrelationIdUtil.getOrGenerate(), OffsetDateTime.now()));
        log.info("Report uploaded: {} by user: {}", saved.getId(), userId);
        return reportMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReport(UUID reportId, UUID userId, boolean isAdmin) {
        Report report = isAdmin
                ? reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId))
                : reportRepository.findByIdAndUserId(reportId, userId).orElseThrow(() -> new ReportNotFoundException(reportId));
        return reportMapper.toResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportSummaryResponse> getUserReports(UUID userId, int page, int size) {
        return PageResponse.from(
                reportRepository.findByUserId(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                        .map(reportMapper::toSummary));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportSummaryResponse> getAllReports(int page, int size) {
        return PageResponse.from(
                reportRepository.findAll(
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                        .map(reportMapper::toSummary));
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId, UUID userId, boolean isAdmin) {
        Report report = isAdmin
                ? reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId))
                : reportRepository.findByIdAndUserId(reportId, userId).orElseThrow(() -> new ReportNotFoundException(reportId));
        fileStorageService.delete(report.getFilePath());
        reportRepository.deleteById(reportId);
        log.info("Report deleted: {}", reportId);
    }

    @Override
    @Transactional
    public void requestAnalysis(UUID reportId, UUID userId) {
        Report report = reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));
        if (report.getStatus() != ReportStatus.PROCESSED) {
            throw new BusinessException("Report must be in PROCESSED state to request analysis",
                    HttpStatus.BAD_REQUEST, "INVALID_STATE");
        }
        report.setStatus(ReportStatus.ANALYSIS_REQUESTED);
        reportRepository.save(report);
        eventPublisher.publishReportUploaded(new ReportUploadedEvent(
                report.getId(), userId, report.getTool().name(), report.getFilePath(),
                CorrelationIdUtil.getOrGenerate(), OffsetDateTime.now()));
        log.info("Analysis requested for report: {}", reportId);
    }

    private PerformanceTool parseToolOrThrow(String tool) {
        try {
            return PerformanceTool.valueOf(tool.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Unsupported tool: " + tool + ". Must be JMETER or GATLING",
                    HttpStatus.BAD_REQUEST, "UNSUPPORTED_TOOL");
        }
    }
}
