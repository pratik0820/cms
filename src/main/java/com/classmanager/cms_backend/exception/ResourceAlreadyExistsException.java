package com.classmanager.cms_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceAlreadyExistsException extends RuntimeException {
    private final String errorCode;
    public ResourceAlreadyExistsException(String message) { super(message); this.errorCode = "ALREADY_EXISTS"; }
    public String getErrorCode() { return errorCode; }
}
