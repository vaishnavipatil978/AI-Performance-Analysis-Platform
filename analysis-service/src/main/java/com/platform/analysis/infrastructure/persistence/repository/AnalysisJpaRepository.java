package com.platform.analysis.infrastructure.persistence.repository;

import com.platform.analysis.infrastructure.persistence.entity.AnalysisEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisJpaRepository extends JpaRepository<AnalysisEntity, UUID> {

    Optional<AnalysisEntity> findByReportId(UUID reportId);

    Page<AnalysisEntity> findByUserId(UUID userId, Pageable pageable);
}
