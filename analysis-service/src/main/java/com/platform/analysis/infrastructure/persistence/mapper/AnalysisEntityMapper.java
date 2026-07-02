package com.platform.analysis.infrastructure.persistence.mapper;

import com.platform.analysis.domain.model.Analysis;
import com.platform.analysis.infrastructure.persistence.entity.AnalysisEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {AnalysisFindingEntityMapper.class})
public interface AnalysisEntityMapper {

    @Mapping(target = "findings", ignore = true)
    AnalysisEntity toEntity(Analysis domain);

    Analysis toDomain(AnalysisEntity entity);
}
