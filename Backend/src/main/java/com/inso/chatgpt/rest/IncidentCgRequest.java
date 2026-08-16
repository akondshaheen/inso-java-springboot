package com.inso.chatgpt.rest;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class IncidentCgRequest {
    private String country;
    private String category;
    private String severity;
    private String description;
    private String status;
}
