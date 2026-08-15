package com.inso.dev.infrastructure;

import com.inso.dev.domain.Incident;

public final class IncidentMapper {

    public static IncidentEntity toIncidentEntity(Incident incident){
        if(incident==null){
            throw new IllegalArgumentException("Incident can not be null");
        }
        IncidentEntity incidentEntity = new IncidentEntity();

        incidentEntity.setId(incident.getId());
        incidentEntity.setName(incident.getName());
        incidentEntity.setStatus(incident.getStatus());
        incidentEntity.setCreatedAt(incident.getCreatedAt());
        incidentEntity.setDetectedInVersion(incident.getDetectedInVersion());
        return incidentEntity;
    }

    public static Incident toIncidentDomain(IncidentEntity incidentEntity){

        if (incidentEntity==null){
            throw new IllegalArgumentException("Incident is not exist");
        }

        return new Incident(incidentEntity.getId(), incidentEntity.getName(), incidentEntity.getDetectedInVersion(), incidentEntity.getStatus(), incidentEntity.getCreatedAt());
    }
}
