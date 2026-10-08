package com.kostas.bookingproject.exceptions;

/** Thrown when an authenticated user is not allowed to perform an action (maps to HTTP 403). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
