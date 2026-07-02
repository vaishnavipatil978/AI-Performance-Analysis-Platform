package com.platform.analysis.domain.repository;

import com.platform.analysis.domain.model.Analysis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface AnalysisRepository {

    Analysis save(Analysis analysis);

    Optional<Analysis> findById(UUID id);

    Optional<Analysis> findByReportId(UUID reportId);

    Page<Analysis> findByUserId(UUID userId, Pageable pageable);

    Page<Analysis> findAll(Pageable pageable);
}
