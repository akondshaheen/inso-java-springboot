package com.inso.learning.taskflow.security;

import com.inso.learning.taskflow.dto.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * WHY DOES A REQUEST WITH NO TOKEN AT ALL COME BACK AS 401, NOT 403?
 * =============================================================================
 *
 * A COMMON, EASY-TO-MISS SPRING SECURITY DEFAULT
 * -------------------------------------------------------------------------
 * By default, Spring Security still assigns every request SOME identity -
 * even one with no Authorization header at all is treated as an
 * "anonymous" user. Because of this, a plain, unconfigured Spring Security
 * setup will respond to an unauthenticated request to a protected endpoint
 * with 403 Forbidden (an AccessDeniedException, "anonymous is not
 * authenticated"), not 401 Unauthorized - which is technically true, but
 * not what REST conventions usually expect, and not what most interviewers
 * expect you to say either. We fix this here with our own
 * AuthenticationEntryPoint, which Spring Security calls specifically when
 * an unauthenticated caller (no token, or an invalid one) tries to reach
 * an endpoint that requires authentication.
 *
 * WHY CAN THIS CLASS NOT JUST THROW AN EXCEPTION AND LET
 * GlobalExceptionHandler HANDLE IT?
 * -------------------------------------------------------------------------
 * @RestControllerAdvice only intercepts exceptions thrown while Spring
 * MVC is dispatching a request to a @Controller method. Spring Security's
 * filters run BEFORE that dispatch even happens, so an AuthenticationEntryPoint
 * must write the HTTP response directly using the raw Servlet API
 * (HttpServletResponse) instead of simply returning a value or throwing.
 * We reuse the exact same ApiErrorResponse shape here so a client still
 * receives one consistent error format everywhere in this API, whether the
 * failure happened inside a controller or inside the security filter
 * chain.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiErrorResponse errorBody = new ApiErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Authentication is required to access this resource.",
                request.getRequestURI(),
                null);

        objectMapper.writeValue(response.getOutputStream(), errorBody);
    }
}
