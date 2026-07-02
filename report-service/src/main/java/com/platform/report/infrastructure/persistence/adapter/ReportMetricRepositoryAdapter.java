package com.platform.report.infrastructure.persistence.adapter;

import com.platform.report.domain.model.ReportMetric;
import com.platform.report.domain.repository.ReportMetricRepository;
import com.platform.report.infrastructure.persistence.entity.ReportMetricEntity;
import com.platform.report.infrastructure.persistence.mapper.ReportMetricEntityMapper;
import com.platform.report.infrastructure.persistence.repository.ReportJpaRepository;
import com.platform.report.infrastructure.persistence.repository.ReportMetricJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportMetricRepositoryAdapter implements ReportMetricRepository {

    private final ReportMetricJpaRepository jpaRepository;
    private final ReportJpaRepository reportJpaRepository;
    private final ReportMetricEntityMapper mapper;

    @Override
    public List<ReportMetric> saveAll(List<ReportMetric> metrics) {
        List<ReportMetricEntity> entities = metrics.stream().map(m -> {
            ReportMetricEntity entity = mapper.toEntity(m);
            if (m.getReportId() != null) {
                reportJpaRepository.findById(m.getReportId())
                        .ifPresent(entity::setReport);
            }
            return entity;
        }).collect(Collectors.toList());
        return jpaRepository.saveAll(entities).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportMetric> findByReportId(UUID reportId) {
        return jpaRepository.findByReportId(reportId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByReportId(UUID reportId) {
        jpaRepository.deleteByReportId(reportId);
    }
}
