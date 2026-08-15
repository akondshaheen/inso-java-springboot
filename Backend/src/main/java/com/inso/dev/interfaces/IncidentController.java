package com.inso.dev.interfaces;

import com.inso.dev.domain.Incident;
import com.inso.dev.interfaces.rest.IncidentRequest;
import com.inso.dev.interfaces.rest.IncidentResponse;
import com.inso.dev.services.IncidentService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/api/incidents")
public class IncidentController {
    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentResponse create(@RequestBody IncidentRequest incidentRequest){
            Incident saved = incidentService.create(incidentRequest.getName(), incidentRequest.getDetectedInVersion(), incidentRequest.getStatus());

            IncidentResponse incidentResponse = new IncidentResponse(
                    saved.getName(),
                    saved.getDetectedInVersion(),
                    saved.getCreatedAt()
            );

            return incidentResponse;
        }

    @GetMapping
    public List<Incident> getAll(){
        return incidentService.getAllIncidents();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id){
        incidentService.deleteIncidentById(id);
    }
}
