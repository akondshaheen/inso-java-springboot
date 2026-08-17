package com.inso.learning.taskflow.dto.response;

import java.time.LocalDateTime;
import java.util.List;

/**
 * A CONSISTENT SHAPE FOR EVERY ERROR RESPONSE
 * -------------------------------------------------------------------------
 * Without a shared error format, every endpoint might return errors
 * differently, forcing API clients to write special-case handling per
 * endpoint. This record defines ONE shape used by every error response in
 * the application - wired up in the global exception handler
 * (@ControllerAdvice) in a later stage. "fieldErrors" is populated only
 * for validation failures (400 responses listing which fields failed and
 * why); it stays null for other kinds of errors, like 404 or 409.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> fieldErrors
) {
}
