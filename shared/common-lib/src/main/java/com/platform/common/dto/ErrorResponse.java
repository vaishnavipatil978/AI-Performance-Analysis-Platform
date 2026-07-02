package com.platform.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<ValidationError> validationErrors
) {
    public record ValidationError(String field, String message) {}
}
