package com.inso.chatgpt.rest;

import com.inso.chatgpt.domain.IncidentCg;
import com.inso.chatgpt.infrastructure.IncidentCgRepository;
import com.inso.dev.infrastructure.IncidentRepository;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IncidentCgService {

    IncidentCgRepository incidentRepository;

    public IncidentCgService(IncidentCgRepository incidentRepository){
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public IncidentCg create(String idempotencyKey,String country, String category, String severity, String description, String status){
        Optional<IncidentCg> existingIncident = incidentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingIncident.isPresent()) {
            return existingIncident.get();
        }

        IncidentCg incidentCg = new IncidentCg(idempotencyKey,country,category, severity, description, status);
        try {
            return incidentRepository.save(incidentCg);
        } catch (DataIntegrityViolationException e) {
            return incidentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> e);
        }
    }

}
