package com.inso.chatgpt.rest;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class IncidentCgResponse {
    private UUID id;
    private String country;
    private String category;
    private String severity;
    private String description;
    private String status;
    private Instant createdAt;
}
