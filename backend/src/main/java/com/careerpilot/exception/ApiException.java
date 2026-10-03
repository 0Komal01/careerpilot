package com.careerpilot.exception;

import org.springframework.http.HttpStatus;

/** Base class for errors we raise on purpose. The global handler turns it into clean JSON. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    public ApiException(HttpStatus status, String message) { super(message); this.status = status; }
    public HttpStatus getStatus() { return status; }
}
