package com.platform.report.infrastructure.persistence.adapter;

import com.platform.report.domain.model.Report;
import com.platform.report.domain.repository.ReportRepository;
import com.platform.report.infrastructure.persistence.mapper.ReportEntityMapper;
import com.platform.report.infrastructure.persistence.repository.ReportJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ReportRepositoryAdapter implements ReportRepository {

    private final ReportJpaRepository jpaRepository;
    private final ReportEntityMapper mapper;

    @Override
    public Report save(Report report) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(report)));
    }

    @Override
    public Optional<Report> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Report> findByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public Page<Report> findByUserId(UUID userId, Pageable pageable) {
        return jpaRepository.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Report> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public long countByUserId(UUID userId) {
        return jpaRepository.countByUserId(userId);
    }
}
