package com.platform.report.application.mapper;

import com.platform.report.application.dto.response.ReportMetricResponse;
import com.platform.report.application.dto.response.ReportResponse;
import com.platform.report.application.dto.response.ReportSummaryResponse;
import com.platform.report.domain.model.Report;
import com.platform.report.domain.model.ReportMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {

    @Mapping(target = "tool", expression = "java(report.getTool().name())")
    @Mapping(target = "status", expression = "java(report.getStatus().name())")
    ReportResponse toResponse(Report report);

    @Mapping(target = "tool", expression = "java(report.getTool().name())")
    @Mapping(target = "status", expression = "java(report.getStatus().name())")
    ReportSummaryResponse toSummary(Report report);

    ReportMetricResponse toMetricResponse(ReportMetric metric);
}
