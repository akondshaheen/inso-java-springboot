package com.inso.dev.interfaces.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class IncidentResponse {
    private String name;
    private String detectedInVersion;
    private Instant createdAt;
}
