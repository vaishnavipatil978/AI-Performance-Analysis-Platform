package com.platform.report.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UploadReportRequest(
        @NotBlank String title,
        String description,
        @NotNull String tool
) {}
