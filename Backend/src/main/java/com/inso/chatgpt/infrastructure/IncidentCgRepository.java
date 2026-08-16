package com.inso.chatgpt.infrastructure;

import com.inso.chatgpt.domain.IncidentCg;

import java.util.List;
import java.util.Optional;

public interface IncidentCgRepository {
    Optional<IncidentCg> findByIdempotencyKey(String key);
    public IncidentCg save(IncidentCg incidentCg);
    public List<IncidentCg> getAll();
    public void delete(IncidentCg incidentCg);
}
