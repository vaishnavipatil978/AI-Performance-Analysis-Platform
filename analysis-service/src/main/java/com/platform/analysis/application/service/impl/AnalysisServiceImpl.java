package com.platform.analysis.application.service.impl;

import com.platform.common.dto.PageResponse;
import com.platform.common.util.CorrelationIdUtil;
import com.platform.analysis.application.dto.response.AnalysisResponse;
import com.platform.analysis.application.mapper.AnalysisMapper;
import com.platform.analysis.application.service.AnalysisService;
import com.platform.analysis.domain.event.AnalysisCompletedEvent;
import com.platform.analysis.domain.event.ReportUploadedEvent;
import com.platform.analysis.domain.exception.AnalysisNotFoundException;
import com.platform.analysis.domain.model.Analysis;
import com.platform.analysis.domain.model.AnalysisFinding;
import com.platform.analysis.domain.model.AnalysisStatus;
import com.platform.analysis.domain.repository.AnalysisRepository;
import com.platform.analysis.infrastructure.ai.PerformanceAnalysisAiService;
import com.platform.analysis.infrastructure.messaging.AnalysisEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final AnalysisMapper analysisMapper;
    private final PerformanceAnalysisAiService aiService;
    private final AnalysisEventPublisher eventPublisher;

    @Override
    public void processReportUploadedEvent(ReportUploadedEvent event) {
        log.info("Processing report uploaded event for reportId={}", event.reportId());
        Analysis analysis = Analysis.builder()
                .reportId(event.reportId())
                .userId(event.userId())
                .status(AnalysisStatus.IN_PROGRESS)
                .build();
        Analysis saved = analysisRepository.save(analysis);
        try {
            List<AnalysisFinding> findings = aiService.analyseReport(event.filePath(), event.tool());
            saved.setFindings(findings);
            saved.setStatus(AnalysisStatus.COMPLETED);
            saved.setAiModel(aiService.getModelName());
            Analysis completed = analysisRepository.save(saved);
            eventPublisher.publishAnalysisCompleted(new AnalysisCompletedEvent(
                    completed.getId(), event.reportId(), event.userId(),
                    findings.size(), AnalysisStatus.COMPLETED.name(),
                    CorrelationIdUtil.getOrGenerate(), OffsetDateTime.now()));
            log.info("Analysis completed for reportId={}, findings={}", event.reportId(), findings.size());
        } catch (Exception e) {
            log.error("AI analysis failed for reportId={}", event.reportId(), e);
            saved.setStatus(AnalysisStatus.FAILED);
            analysisRepository.save(saved);
            eventPublisher.publishAnalysisCompleted(new AnalysisCompletedEvent(
                    saved.getId(), event.reportId(), event.userId(),
                    0, AnalysisStatus.FAILED.name(),
                    CorrelationIdUtil.getOrGenerate(), OffsetDateTime.now()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysis(UUID analysisId, UUID userId, boolean isAdmin) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new AnalysisNotFoundException(analysisId));
        if (!isAdmin && !analysis.getUserId().equals(userId)) {
            throw new AnalysisNotFoundException(analysisId);
        }
        return analysisMapper.toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysisByReportId(UUID reportId, UUID userId, boolean isAdmin) {
        Analysis analysis = analysisRepository.findByReportId(reportId)
                .orElseThrow(() -> new AnalysisNotFoundException("reportId", reportId.toString()));
        if (!isAdmin && !analysis.getUserId().equals(userId)) {
            throw new AnalysisNotFoundException("reportId", reportId.toString());
        }
        return analysisMapper.toResponse(analysis);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AnalysisResponse> getUserAnalyses(UUID userId, int page, int size) {
        return PageResponse.from(analysisRepository.findByUserId(userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(analysisMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AnalysisResponse> getAllAnalyses(int page, int size) {
        return PageResponse.from(analysisRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(analysisMapper::toResponse));
    }
}
