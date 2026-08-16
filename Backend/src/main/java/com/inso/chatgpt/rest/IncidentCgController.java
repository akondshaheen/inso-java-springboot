package com.inso.chatgpt.rest;

import com.inso.chatgpt.domain.IncidentCg;
import com.inso.dev.interfaces.rest.IncidentRequest;
import com.inso.dev.interfaces.rest.IncidentResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IncidentCgController {
    private final IncidentCgService incidentCgService;

    public IncidentCgController(IncidentCgService incidentCgService) {
        this.incidentCgService = incidentCgService;
    }


    @PostMapping("/api/incident/create")
    public IncidentCgResponse createIncident(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody IncidentCgRequest incidentCgRequest){
        IncidentCg incidentCg = incidentCgService.create(idempotencyKey,incidentCgRequest.getCountry(), incidentCgRequest.getCategory(), incidentCgRequest.getSeverity(), incidentCgRequest.getDescription(), incidentCgRequest.getStatus());

        IncidentCgResponse incidentResponse = new IncidentCgResponse(incidentCg.getId(),incidentCg.getCountry(), incidentCg.getCategory(), incidentCg.getSeverity(), incidentCg.getDescription(), incidentCg.getStatus(), incidentCg.getCreatedAt());
        return incidentResponse;
    }

}
