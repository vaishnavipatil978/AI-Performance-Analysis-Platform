package com.platform.analysis.infrastructure.persistence.mapper;

import com.platform.analysis.domain.model.AnalysisFinding;
import com.platform.analysis.infrastructure.persistence.entity.AnalysisFindingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisFindingEntityMapper {

    @Mapping(target = "analysis", ignore = true)
    AnalysisFindingEntity toEntity(AnalysisFinding domain);

    @Mapping(target = "analysisId", expression = "java(entity.getAnalysis() != null ? entity.getAnalysis().getId() : null)")
    AnalysisFinding toDomain(AnalysisFindingEntity entity);
}
