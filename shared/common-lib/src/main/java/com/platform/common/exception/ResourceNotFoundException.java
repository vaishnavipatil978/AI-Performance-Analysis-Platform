package com.platform.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {

    public ResourceNotFoundException(String resource, String id) {
        super(resource + " not found: " + id, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(resource + " not found with " + field + ": " + value, HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}
