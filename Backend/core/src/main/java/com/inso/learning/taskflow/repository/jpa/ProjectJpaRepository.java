package com.inso.learning.taskflow.repository.jpa;

import com.inso.learning.taskflow.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * "findByOwnerId" reads as "find every ProjectEntity whose owner has this
 * id" - Spring Data automatically writes the join needed to check the
 * related UserEntity's id, without us writing any SQL by hand.
 */
public interface ProjectJpaRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> findByOwnerId(Long ownerId);
}
