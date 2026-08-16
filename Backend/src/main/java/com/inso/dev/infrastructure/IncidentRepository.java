package com.inso.dev.infrastructure;

import com.inso.dev.domain.IncidentDemo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {
    IncidentDemo save(IncidentDemo incidentDemo);
    List<IncidentDemo> getAllIncidents();
    Optional<IncidentDemo> findByStatus(String status);
    IncidentDemo update (IncidentDemo incidentDemo);
    void deleteIncidentById(UUID id);
}
