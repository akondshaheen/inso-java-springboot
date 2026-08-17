package com.inso.learning.taskflow.exception;

/**
 * Thrown when a user tries to perform an action they are not allowed to
 * perform (for example, editing a project they do not own). This maps to
 * an HTTP 403 Forbidden in the global exception handler, which is
 * different from 401 Unauthorized - see AuthorizationException's Javadoc
 * note below for the distinction, an extremely common interview question.
 *
 * AUTHENTICATION VS AUTHORIZATION
 * -------------------------------------------------------------------------
 * Authentication answers "who are you?" - proving identity, usually with a
 * username/password or a token. Authorization answers "what are you
 * allowed to do?" - given a known identity, deciding whether a specific
 * action is permitted. A request can fail authentication (401: we do not
 * know who you are, or your credentials are invalid) or fail authorization
 * (403: we know who you are, but you are not allowed to do this).
 */
public class AuthorizationException extends RuntimeException {

    public AuthorizationException(String message) {
        super(message);
    }
}
