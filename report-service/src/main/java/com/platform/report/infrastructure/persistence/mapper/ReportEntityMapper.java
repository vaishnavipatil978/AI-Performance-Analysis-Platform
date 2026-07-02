package com.platform.report.infrastructure.persistence.mapper;

import com.platform.report.domain.model.Report;
import com.platform.report.infrastructure.persistence.entity.ReportEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ReportMetricEntityMapper.class})
public interface ReportEntityMapper {

    @Mapping(target = "metrics", ignore = true)
    ReportEntity toEntity(Report domain);

    Report toDomain(ReportEntity entity);
}
