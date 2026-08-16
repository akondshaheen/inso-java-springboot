package com.inso.dev.infrastructure;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "incidentDemo")
@Data
public class IncidentEntity {
    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "detected_in_version")
    private String detectedInVersion;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private Instant createdAt;
}