package com.platform.analysis.application.mapper;

import com.platform.analysis.application.dto.response.AnalysisResponse;
import com.platform.analysis.application.dto.response.FindingResponse;
import com.platform.analysis.domain.model.Analysis;
import com.platform.analysis.domain.model.AnalysisFinding;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnalysisMapper {

    @Mapping(target = "status", expression = "java(analysis.getStatus().name())")
    AnalysisResponse toResponse(Analysis analysis);

    @Mapping(target = "severity", expression = "java(finding.getSeverity().name())")
    FindingResponse toFindingResponse(AnalysisFinding finding);
}
