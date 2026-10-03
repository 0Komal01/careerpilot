package com.careerpilot.exception;

import org.springframework.http.HttpStatus;

public class DuplicateApplicationException extends ApiException {
    public DuplicateApplicationException(String message) { super(HttpStatus.CONFLICT, message); }
}
