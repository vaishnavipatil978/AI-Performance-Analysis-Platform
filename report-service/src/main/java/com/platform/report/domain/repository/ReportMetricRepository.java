package com.platform.report.domain.repository;

import com.platform.report.domain.model.ReportMetric;

import java.util.List;
import java.util.UUID;

public interface ReportMetricRepository {

    List<ReportMetric> saveAll(List<ReportMetric> metrics);

    List<ReportMetric> findByReportId(UUID reportId);

    void deleteByReportId(UUID reportId);
}
