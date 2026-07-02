package com.platform.report.infrastructure.persistence.repository;

import com.platform.report.infrastructure.persistence.entity.ReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportJpaRepository extends JpaRepository<ReportEntity, UUID> {

    Page<ReportEntity> findByUserId(UUID userId, Pageable pageable);

    Optional<ReportEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
