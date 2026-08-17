package com.inso.learning.taskflow.exception;

/**
 * CHECKED VS UNCHECKED EXCEPTIONS
 * -------------------------------------------------------------------------
 * Java has two categories of exceptions. CHECKED exceptions (any subclass
 * of Exception that is not a RuntimeException, like IOException) must
 * either be caught with try/catch or declared with "throws" on the method
 * signature - the compiler enforces this. UNCHECKED exceptions (any
 * subclass of RuntimeException) are not enforced by the compiler at all;
 * a method can throw one without declaring it.
 *
 * We deliberately make ResourceNotFoundException UNCHECKED (it extends
 * RuntimeException) because "the requested User/Project/Task does not
 * exist" is a normal, expected outcome for a REST API to communicate as a
 * 404 response - forcing every single caller up the chain (repository ->
 * service -> controller) to catch or re-declare it would add a lot of
 * repetitive, low-value code. Instead, we let it propagate all the way up
 * to a single, centralised handler (@ControllerAdvice, in a later stage),
 * which converts it into a proper HTTP 404 response.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
