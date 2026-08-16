package com.inso.chatgpt.infrastructure;

import com.inso.chatgpt.domain.IncidentCg;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class IncidentCgJpaRepository implements IncidentCgRepository {
    SpringDataIncidentCgJpaRepository springDataIncidentCgJpaRepository;

    public IncidentCgJpaRepository(SpringDataIncidentCgJpaRepository springDataIncidentCgJpaRepository){
        this.springDataIncidentCgJpaRepository = springDataIncidentCgJpaRepository;
    }


    @Override
    public Optional<IncidentCg> findByIdempotencyKey(String key) {
        return springDataIncidentCgJpaRepository.findByIdempotencyKey(key)
                .map(IncidentDomainEntityCgMapper::toIncidentCg);
    }

    @Override
    public IncidentCg save(IncidentCg incidentCg) {

        IncidentCgEntity incidentCgEntity  = IncidentDomainEntityCgMapper.toIncidentCgEntity(incidentCg);
        IncidentCgEntity save  = springDataIncidentCgJpaRepository.save(incidentCgEntity);

        return IncidentDomainEntityCgMapper.toIncidentCg(save);
    }

    @Override
    public List<IncidentCg> getAll() {
        return springDataIncidentCgJpaRepository
                .findAll()
                .stream()
                .map(incident->IncidentDomainEntityCgMapper.toIncidentCg(incident))
                .toList();
    }

    @Override
    public void delete(IncidentCg incidentCg) {

    }
}
