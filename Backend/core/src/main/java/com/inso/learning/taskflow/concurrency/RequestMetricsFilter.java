package com.inso.learning.taskflow.concurrency;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * A servlet Filter that runs once per incoming HTTP request and records
 * which endpoint was called, using the thread-safe RequestMetrics
 * component above. This follows the exact same "OncePerRequestFilter"
 * pattern already used by JwtAuthenticationFilter: Spring Boot
 * auto-registers any Filter bean to run on every request that reaches the
 * embedded Tomcat server, BEFORE Spring MVC even chooses which
 * @RestController method should handle it.
 *
 * WHY BUILD THIS AS A FILTER INSTEAD OF ADDING CODE TO EVERY CONTROLLER
 * METHOD?
 * -------------------------------------------------------------------------
 * This is the same "cross-cutting concern" idea already explained for
 * GlobalExceptionHandler: counting requests has nothing to do with any
 * single controller's business responsibility, so writing
 * "metrics.increment(...)" inside every single controller method would
 * be repetitive and easy to forget on a new endpoint. A filter lets us
 * write this logic exactly once, in exactly one place, and have it apply
 * automatically to every request.
 *
 * WHY DOES THIS FILTER MATTER FOR CONCURRENCY SPECIFICALLY?
 * -------------------------------------------------------------------------
 * Tomcat uses a POOL OF THREADS to serve requests, so this filter's
 * doFilterInternal(...) method can genuinely be RUNNING AT THE SAME TIME
 * on several different threads if several requests arrive close together.
 * That is exactly the scenario RequestMetrics was built to handle safely.
 */
@Component
public class RequestMetricsFilter extends OncePerRequestFilter {

    private final RequestMetrics requestMetrics;

    public RequestMetricsFilter(RequestMetrics requestMetrics) {
        this.requestMetrics = requestMetrics;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // We record "METHOD path" (for example "GET /api/tasks") so that
        // the same URL called with different HTTP methods is counted
        // separately - a GET and a POST to the same path are different
        // operations with very different meanings.
        String endpointKey = request.getMethod() + " " + request.getRequestURI();
        requestMetrics.increment(endpointKey);

        // filterChain.doFilter(...) passes control to the NEXT filter in
        // the chain (eventually reaching Spring MVC and our controller).
        // Forgetting this call would silently stop every request dead in
        // its tracks - a common mistake when writing a filter for the
        // first time.
        filterChain.doFilter(request, response);
    }
}
