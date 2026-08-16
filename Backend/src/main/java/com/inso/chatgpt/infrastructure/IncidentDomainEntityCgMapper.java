package com.inso.chatgpt.infrastructure;

import com.inso.chatgpt.domain.IncidentCg;

public class IncidentDomainEntityCgMapper {

    public static IncidentCgEntity toIncidentCgEntity(IncidentCg incident){
        IncidentCgEntity incidentCgEntity = new IncidentCgEntity();

        incidentCgEntity.setId(incident.getId());
        incidentCgEntity.setIdempotencyKey(incident.getIdempotencyKey());
        incidentCgEntity.setCountry(incident.getCountry());
        incidentCgEntity.setCategory(incident.getCategory());
        incidentCgEntity.setSeverity(incident.getSeverity());
        incidentCgEntity.setDescription(incident.getDescription());
        incidentCgEntity.setStatus(incident.getStatus());
        incidentCgEntity.setCreatedAt(incident.getCreatedAt());

        return incidentCgEntity;
    }

    public static IncidentCg toIncidentCg(IncidentCgEntity incidentCgEntity){
        return new IncidentCg(incidentCgEntity.getId(),
                incidentCgEntity.getIdempotencyKey(),
                incidentCgEntity.getCountry(),
                incidentCgEntity.getCategory(),
                incidentCgEntity.getSeverity(),
                incidentCgEntity.getDescription(),
                incidentCgEntity.getStatus(),
                incidentCgEntity.getCreatedAt());
    }
}
