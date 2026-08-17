package com.inso.learning.taskflow.exception;

import com.inso.learning.taskflow.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * =============================================================================
 * GLOBAL EXCEPTION HANDLING WITH @RestControllerAdvice
 * =============================================================================
 *
 * WHY NOT JUST WRAP EVERY CONTROLLER METHOD IN A try/catch BLOCK?
 * -------------------------------------------------------------------------
 * We could catch exceptions individually inside every controller method,
 * but that would mean repeating the same error-formatting logic dozens of
 * times across the codebase, and it would be very easy to forget a case
 * or format one endpoint's errors slightly differently from another's.
 * @RestControllerAdvice lets us write that logic exactly ONCE, in one
 * place, and Spring automatically applies it to every controller in the
 * application - this is a form of "cross-cutting concern" (a concern
 * that touches many otherwise-unrelated parts of the code), and handling
 * it centrally is a common and valuable Spring pattern.
 *
 * WHAT IS @RestControllerAdvice AND HOW DOES IT WORK BEHIND THE SCENES?
 * -------------------------------------------------------------------------
 * @RestControllerAdvice is @ControllerAdvice plus @ResponseBody (just like
 * @RestController is @Controller plus @ResponseBody). Spring registers
 * this class as a special bean that the DispatcherServlet consults
 * whenever a controller method throws an exception. Spring looks for an
 * @ExceptionHandler method in this class whose declared exception type
 * matches (or is a superclass of) the thrown exception, and calls it
 * instead of letting the exception crash the request with a raw Tomcat
 * error page.
 *
 * WHY DOES EVERY HANDLER RETURN THE SAME "ApiErrorResponse" SHAPE?
 * -------------------------------------------------------------------------
 * A predictable response body means any client (a frontend, a mobile app,
 * another backend service) can write ONE piece of error-handling code
 * that works for every endpoint, instead of guessing the shape of each
 * individual error.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // A LOGGER, AND WHY WE LOG THE UNEXPECTED-EXCEPTION CASE SPECIFICALLY
    // -------------------------------------------------------------------
    // We deliberately hide unexpected error DETAILS from the CLIENT (see
    // handleUnexpected below), but that does not mean the details should
    // disappear entirely - our own team still needs them to diagnose what
    // actually went wrong. Logging the full exception on the SERVER side,
    // while returning a generic message to the CALLER, gives us both:
    // useful diagnostics for developers, and no information leakage to
    // whoever sent the request.
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /**
     * 409 CONFLICT is the correct status when the request is well-formed
     * and the client is authorized, but the operation cannot proceed
     * because it conflicts with the current state of the server's data -
     * for example, trying to register a second user with an email address
     * that is already taken. This is different from 400 Bad Request,
     * which means the request itself was malformed or failed validation
     * before we ever considered the database's current state.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request, null);
    }

    /**
     * 403 FORBIDDEN vs 401 UNAUTHORIZED
     * -------------------------------------------------------------------
     * 401 Unauthorized really means "unauthenticated": the server does not
     * know who you are at all (no valid credentials were supplied). 403
     * Forbidden means the server knows exactly who you are, but you are
     * not allowed to perform this specific action - which is exactly the
     * case here, where AuthorizationException is thrown for a known user
     * who simply is not the project's owner or an admin.
     */
    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthorization(AuthorizationException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request, null);
    }

    /**
     * 401 UNAUTHORIZED: THE SERVER DOES NOT KNOW WHO YOU ARE
     * -------------------------------------------------------------------
     * Spring Security's BadCredentialsException is thrown by
     * AuthController when a login's email or password is wrong - at that
     * point in the flow, we have no valid identity for this caller at all,
     * which is exactly what 401 communicates. Compare this with 403
     * FORBIDDEN above: 403 is for a caller we ALREADY know the identity of
     * but who lacks permission for this specific action.
     */
    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request, null);
    }

    /**
     * Spring Security throws AccessDeniedException when a role-based rule
     * configured in SecurityConfig (like ".hasRole("ADMIN")") rejects an
     * authenticated-but-not-permitted caller. This is a 403, for the same
     * reason as AuthorizationException above - the caller's identity is
     * known, they simply are not allowed to perform this action.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You do not have permission to perform this action.", request, null);
    }

    /**
     * THIS IS WHERE BEAN VALIDATION FAILURES (@Valid ON A REQUEST DTO)
     * END UP.
     * -------------------------------------------------------------------
     * When @Valid on a controller parameter finds constraint violations
     * (like a blank "name" failing @NotBlank), Spring throws
     * MethodArgumentNotValidException BEFORE our controller method body
     * ever runs. We collect every individual field error into a readable
     * list so the client knows exactly which fields failed and why,
     * instead of just being told "bad request" with no detail.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Validation failed for one or more fields.", request, fieldErrors);
    }

    /**
     * MALFORMED JSON: A 400, NOT A 500
     * -------------------------------------------------------------------
     * Spring throws HttpMessageNotReadableException when the request BODY
     * cannot even be parsed as valid JSON (missing quotes, a trailing
     * comma, or an empty body where one was required). This is squarely
     * the CALLER's mistake - they sent us something we cannot understand
     * - so it deserves a 400 Bad Request, exactly like a failed @Valid
     * check, rather than falling through to our generic 500 handler
     * below, which is reserved for problems on OUR side.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(
            org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Request body is missing or not valid JSON.", request, null);
    }

    /**
     * A CATCH-ALL SAFETY NET
     * -------------------------------------------------------------------
     * Any exception we did not anticipate (a bug, a null pointer, a
     * database connectivity issue) falls through to this handler instead
     * of leaking a raw stack trace to the client. We deliberately do not
     * expose ex.getMessage() here for unexpected errors, because internal
     * error details could leak sensitive information about our system to
     * an attacker; 500 Internal Server Error signals "something went wrong
     * on our side", not "you made a mistake".
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error handling {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", request, null);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message,
                                                     HttpServletRequest request, List<String> fieldErrors) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI(), fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}
