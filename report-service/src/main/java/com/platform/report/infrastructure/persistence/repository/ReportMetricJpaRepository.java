package com.platform.report.infrastructure.persistence.repository;

import com.platform.report.infrastructure.persistence.entity.ReportMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportMetricJpaRepository extends JpaRepository<ReportMetricEntity, UUID> {

    List<ReportMetricEntity> findByReportId(UUID reportId);

    void deleteByReportId(UUID reportId);
}
