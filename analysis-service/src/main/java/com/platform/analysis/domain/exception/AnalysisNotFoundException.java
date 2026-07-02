package com.platform.analysis.domain.exception;

import com.platform.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class AnalysisNotFoundException extends ResourceNotFoundException {

    public AnalysisNotFoundException(UUID id) {
        super("Analysis", "id", id.toString());
    }

    public AnalysisNotFoundException(String field, String value) {
        super("Analysis", field, value);
    }
}
