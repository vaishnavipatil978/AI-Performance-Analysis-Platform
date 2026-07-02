package com.platform.common.web;

import com.platform.common.dto.ErrorResponse;
import com.platform.common.exception.BaseException;
import com.platform.common.util.CorrelationIdUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public abstract class GlobalExceptionHandlerBase {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.error("Business exception [{}]: {} at {}", ex.getErrorCode(), ex.getMessage(), request.getRequestURI());
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                ex.getHttpStatus().value(),
                ex.getHttpStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                CorrelationIdUtil.get(),
                null
        );
        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.ValidationError(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Request validation failed. See validationErrors for details.",
                request.getRequestURI(),
                CorrelationIdUtil.get(),
                validationErrors
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex,
                                                             HttpServletRequest request) {
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "File Too Large",
                "Uploaded file exceeds the maximum allowed size.",
                request.getRequestURI(),
                CorrelationIdUtil.get(),
                null
        );
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI(),
                CorrelationIdUtil.get(),
                null
        );
        return ResponseEntity.internalServerError().body(error);
    }
}
