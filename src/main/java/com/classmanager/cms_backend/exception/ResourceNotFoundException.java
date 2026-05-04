package com.classmanager.cms_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode;
    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " not found with id: " + id); this.errorCode = "NOT_FOUND";
    }
    public ResourceNotFoundException(String message) { super(message); this.errorCode = "NOT_FOUND"; }
    public String getErrorCode() { return errorCode; }
}
