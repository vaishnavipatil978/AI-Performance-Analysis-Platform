package com.platform.report.infrastructure.persistence.mapper;

import com.platform.report.domain.model.ReportMetric;
import com.platform.report.infrastructure.persistence.entity.ReportMetricEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMetricEntityMapper {

    @Mapping(target = "report", ignore = true)
    ReportMetricEntity toEntity(ReportMetric domain);

    @Mapping(target = "reportId", expression = "java(entity.getReport() != null ? entity.getReport().getId() : null)")
    ReportMetric toDomain(ReportMetricEntity entity);
}
