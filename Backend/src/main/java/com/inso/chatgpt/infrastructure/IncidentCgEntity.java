package com.inso.chatgpt.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Table(name = "incident_cg")
@Entity
@Data
public class IncidentCgEntity {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", unique = true)
    private String idempotencyKey;

    @Column(name = "country")
    private String country;

    @Column
    private String category;

    @Column
    private String severity;

    @Column
    private String description;

    @Column
    private String status;

    @Column(name="created_at")
    private Instant createdAt;
}
