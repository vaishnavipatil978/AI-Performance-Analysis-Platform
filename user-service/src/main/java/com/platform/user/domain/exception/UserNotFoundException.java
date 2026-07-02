package com.platform.user.domain.exception;

import com.platform.common.exception.ResourceNotFoundException;

import java.util.UUID;

public class UserNotFoundException extends ResourceNotFoundException {

    public UserNotFoundException(UUID id) {
        super("User", id.toString());
    }

    public UserNotFoundException(String email) {
        super("User", "email: " + email);
    }
}
