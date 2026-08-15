package com.inso.dev.services;
import com.inso.dev.domain.Incident;
import com.inso.dev.infrastructure.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class IncidentService {
    IncidentRepository incidentRepository;
    public IncidentService(IncidentRepository incidentRepository){
        this.incidentRepository = incidentRepository;
    }

    public Incident create(String name, String detectedInVersion, String status){
        Incident incident = new Incident(name, detectedInVersion, status);
        Incident saved = incidentRepository.save(incident);
        return saved;
    }

    public List<Incident> getAllIncidents(){
        return incidentRepository.getAllIncidents();
    }

    public void deleteIncidentById(UUID id){
        incidentRepository.deleteIncidentById(id);
    }
}
