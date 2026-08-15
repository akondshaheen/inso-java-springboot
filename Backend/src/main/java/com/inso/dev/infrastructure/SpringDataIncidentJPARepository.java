package com.inso.dev.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataIncidentJPARepository extends JpaRepository<IncidentEntity, UUID> {
}
