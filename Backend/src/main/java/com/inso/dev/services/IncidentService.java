package com.inso.dev.services;
import com.inso.dev.domain.IncidentDemo;
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

    public IncidentDemo create(String name, String detectedInVersion, String status){
        IncidentDemo incidentDemo = new IncidentDemo(name, detectedInVersion, status);
        IncidentDemo saved = incidentRepository.save(incidentDemo);
        return saved;
    }

    public List<IncidentDemo> getAllIncidents(){
        return incidentRepository.getAllIncidents();
    }

    public void deleteIncidentById(UUID id){
        incidentRepository.deleteIncidentById(id);
    }
}
