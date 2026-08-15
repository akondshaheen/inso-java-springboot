package com.inso.dev.infrastructure;

import com.inso.dev.domain.Incident;
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
    public Incident save(Incident incident) {
        IncidentEntity incidentEntity = IncidentMapper.toIncidentEntity(incident);
        IncidentEntity save = springDataIncidentJPARepository.save(incidentEntity);
        return IncidentMapper.toIncidentDomain(save);
    }

    @Override
    public List<Incident> getAllIncidents() {
        return springDataIncidentJPARepository.findAll().stream().map(IncidentMapper::toIncidentDomain).toList();
    }

    @Override
    public Optional<Incident> findByStatus(String status) {
        return Optional.empty();
    }

    @Override
    public Incident update(Incident incident) {
        return null;
    }

    @Override
    public void deleteIncidentById(UUID id) {
        springDataIncidentJPARepository.deleteById(id);
    }
}
