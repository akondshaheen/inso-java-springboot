package com.inso.chatgpt.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class IncidentCg {
    private UUID id;
    private String idempotencyKey;
    private String country;
    private String category;
    private String severity;
    private String description;
    private String status;
    private Instant createdAt;

    public IncidentCg(String idempotencyKey,String country, String category, String severity, String description, String status){
        this.id = UUID.randomUUID();
        this.idempotencyKey = idempotencyKey;
        this.country = country;
        this.category = category;
        this.severity = severity;
        this.description = description;
        this.status = status;
        createdAt = Instant.now();
    }
}
