package com.inso.chatgpt.infrastructure;

import com.inso.chatgpt.domain.IncidentCg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataIncidentCgJpaRepository extends JpaRepository<IncidentCgEntity, UUID> {
    Optional<IncidentCgEntity> findByIdempotencyKey(String idempotencyKey);
}
