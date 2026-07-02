package com.platform.user.domain.exception;

import com.platform.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends BusinessException {

    public EmailAlreadyExistsException(String email) {
        super("Email already registered: " + email, HttpStatus.CONFLICT, "EMAIL_EXISTS");
    }
}
