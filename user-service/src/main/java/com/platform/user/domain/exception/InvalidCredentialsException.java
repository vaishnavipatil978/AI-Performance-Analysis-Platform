package com.platform.user.domain.exception;

import com.platform.common.exception.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {

    public InvalidCredentialsException() {
        super("Invalid email or password");
    }
}
