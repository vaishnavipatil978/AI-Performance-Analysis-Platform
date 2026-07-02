package com.platform.analysis.infrastructure.persistence.adapter;

import com.platform.analysis.domain.model.Analysis;
import com.platform.analysis.domain.model.AnalysisFinding;
import com.platform.analysis.domain.repository.AnalysisRepository;
import com.platform.analysis.infrastructure.persistence.entity.AnalysisEntity;
import com.platform.analysis.infrastructure.persistence.entity.AnalysisFindingEntity;
import com.platform.analysis.infrastructure.persistence.mapper.AnalysisEntityMapper;
import com.platform.analysis.infrastructure.persistence.mapper.AnalysisFindingEntityMapper;
import com.platform.analysis.infrastructure.persistence.repository.AnalysisJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AnalysisRepositoryAdapter implements AnalysisRepository {

    private final AnalysisJpaRepository jpaRepository;
    private final AnalysisEntityMapper mapper;
    private final AnalysisFindingEntityMapper findingMapper;

    @Override
    @Transactional
    public Analysis save(Analysis analysis) {
        AnalysisEntity entity = mapper.toEntity(analysis);
        if (analysis.getFindings() != null) {
            List<AnalysisFindingEntity> findingEntities = analysis.getFindings().stream()
                    .map(f -> {
                        AnalysisFindingEntity fe = findingMapper.toEntity(f);
                        fe.setAnalysis(entity);
                        return fe;
                    })
                    .collect(Collectors.toList());
            entity.setFindings(findingEntities);
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Analysis> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Analysis> findByReportId(UUID reportId) {
        return jpaRepository.findByReportId(reportId).map(mapper::toDomain);
    }

    @Override
    public Page<Analysis> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Analysis> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }
}
