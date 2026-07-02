package com.platform.report.domain.exception;

import com.platform.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class ReportNotFoundException extends ResourceNotFoundException {

    public ReportNotFoundException(UUID id) {
        super("Report", "id", id.toString());
    }
}
