package com.inso.learning.taskflow.exception;

/**
 * Thrown when an operation would create a duplicate that must be unique
 * (for example, signing up with an email address that is already
 * registered). This is unchecked for the same reason as
 * ResourceNotFoundException: it represents a normal, expected outcome
 * that a REST API needs to turn into a specific HTTP status (409
 * Conflict), not a programming error that every caller must handle
 * individually.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
