package com.classmanager.cms_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {
    private final String errorCode;
    public UnauthorizedException(String message) { super(message); this.errorCode = "UNAUTHORIZED"; }
    public UnauthorizedException(String message, String errorCode) { super(message); this.errorCode = errorCode; }
    public String getErrorCode() { return errorCode; }
}
