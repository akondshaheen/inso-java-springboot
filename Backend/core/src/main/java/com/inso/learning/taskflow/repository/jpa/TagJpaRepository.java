package com.inso.learning.taskflow.repository.jpa;

import com.inso.learning.taskflow.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagJpaRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByName(String name);
}
