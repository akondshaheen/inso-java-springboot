package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.concurrency.RequestMetrics;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * A tiny admin-only endpoint that exposes the request counts collected by
 * RequestMetrics/RequestMetricsFilter. Only an ADMIN may read this (see
 * SecurityConfig's rule for "/api/metrics") - request counts can reveal
 * information about how the system is used, so we treat them as
 * operational data rather than something every caller should see.
 *
 * This is intentionally simple. Spring Boot Actuator (introduced in the
 * performance stage) provides a much richer, production-grade metrics
 * endpoint out of the box - we still build this small hand-written
 * version first so the underlying idea (a thread-safe counter, updated by
 * a filter, read by a controller) is not hidden behind a library.
 */
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {

    private final RequestMetrics requestMetrics;

    public MetricsController(RequestMetrics requestMetrics) {
        this.requestMetrics = requestMetrics;
    }

    @GetMapping
    public Map<String, Long> getRequestCounts() {
        return requestMetrics.snapshot();
    }
}
