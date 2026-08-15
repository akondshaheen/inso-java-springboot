package com.inso.dev.interfaces.rest;

import lombok.Data;

@Data
public class IncidentRequest {
    private String name;
    private String detectedInVersion;
    private String status;
}
