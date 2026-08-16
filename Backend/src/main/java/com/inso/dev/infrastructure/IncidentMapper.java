package com.inso.dev.infrastructure;

import com.inso.dev.domain.IncidentDemo;

public final class IncidentMapper {

    public static IncidentEntity toIncidentEntity(IncidentDemo incidentDemo){
        if(incidentDemo ==null){
            throw new IllegalArgumentException("Incident can not be null");
        }
        IncidentEntity incidentEntity = new IncidentEntity();

        incidentEntity.setId(incidentDemo.getId());
        incidentEntity.setName(incidentDemo.getName());
        incidentEntity.setStatus(incidentDemo.getStatus());
        incidentEntity.setCreatedAt(incidentDemo.getCreatedAt());
        incidentEntity.setDetectedInVersion(incidentDemo.getDetectedInVersion());
        return incidentEntity;
    }

    public static IncidentDemo toIncidentDomain(IncidentEntity incidentEntity){

        if (incidentEntity==null){
            throw new IllegalArgumentException("Incident is not exist");
        }

        return new IncidentDemo(incidentEntity.getId(), incidentEntity.getName(), incidentEntity.getDetectedInVersion(), incidentEntity.getStatus(), incidentEntity.getCreatedAt());
    }
}
