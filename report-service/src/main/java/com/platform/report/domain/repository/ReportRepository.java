package com.platform.report.domain.repository;

import com.platform.report.domain.model.Report;
import com.platform.report.domain.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepository {

    Report save(Report report);

    Optional<Report> findById(UUID id);

    Optional<Report> findByIdAndUserId(UUID id, UUID userId);

    Page<Report> findByUserId(UUID userId, Pageable pageable);

    Page<Report> findAll(Pageable pageable);

    void deleteById(UUID id);

    long countByUserId(UUID userId);
}
