package com.inso.dev.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class Incident {
    private UUID id;
    private String name;
    private String detectedInVersion;
    private String status;
    private Instant createdAt;

    public Incident(String name, String detectedInVersion, String status){
        id=UUID.randomUUID();
        this.name = name;
        this.detectedInVersion = detectedInVersion;
        this.status = status;
        createdAt=Instant.now();
    }
}
