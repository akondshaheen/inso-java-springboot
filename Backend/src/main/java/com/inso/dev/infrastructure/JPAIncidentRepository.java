package com.inso.dev.infrastructure;

import com.inso.dev.domain.IncidentDemo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JPAIncidentRepository implements IncidentRepository{

    private final SpringDataIncidentJPARepository springDataIncidentJPARepository;

    public JPAIncidentRepository(SpringDataIncidentJPARepository springDataIncidentJPARepository) {
        this.springDataIncidentJPARepository = springDataIncidentJPARepository;
    }

    @Override
    public IncidentDemo save(IncidentDemo incidentDemo) {
        IncidentEntity incidentEntity = IncidentMapper.toIncidentEntity(incidentDemo);
        IncidentEntity save = springDataIncidentJPARepository.save(incidentEntity);
        return IncidentMapper.toIncidentDomain(save);
    }

    @Override
    public List<IncidentDemo> getAllIncidents() {
        return springDataIncidentJPARepository.findAll().stream().map(IncidentMapper::toIncidentDomain).toList();
    }

    @Override
    public Optional<IncidentDemo> findByStatus(String status) {
        return Optional.empty();
    }

    @Override
    public IncidentDemo update(IncidentDemo incidentDemo) {
        return null;
    }

    @Override
    public void deleteIncidentById(UUID id) {
        springDataIncidentJPARepository.deleteById(id);
    }
}
