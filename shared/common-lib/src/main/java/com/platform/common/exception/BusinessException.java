package com.platform.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends BaseException {

    public BusinessException(String message, HttpStatus status) {
        super(message, status, "BUSINESS_ERROR");
    }

    public BusinessException(String message, HttpStatus status, String errorCode) {
        super(message, status, errorCode);
    }
}
