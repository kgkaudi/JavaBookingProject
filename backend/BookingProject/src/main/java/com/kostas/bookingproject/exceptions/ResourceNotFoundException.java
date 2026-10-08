package com.kostas.bookingproject.exceptions;

/** Thrown when a requested entity does not exist (maps to HTTP 404). */
public class ResourceNotFoundException extends IllegalArgumentException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
