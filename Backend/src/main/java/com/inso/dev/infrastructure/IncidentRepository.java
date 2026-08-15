package com.inso.dev.infrastructure;

import com.inso.dev.domain.Incident;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidentRepository {
    Incident save(Incident incident);
    List<Incident> getAllIncidents();
    Optional<Incident> findByStatus(String status);
    Incident update (Incident incident);
    void deleteIncidentById(UUID id);
}
