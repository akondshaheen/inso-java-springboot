package com.inso.learning.taskflow.repository.impl;

import com.inso.learning.taskflow.domain.Project;
import com.inso.learning.taskflow.entity.ProjectEntity;
import com.inso.learning.taskflow.entity.UserEntity;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.mapper.ProjectMapper;
import com.inso.learning.taskflow.repository.ProjectRepository;
import com.inso.learning.taskflow.repository.jpa.ProjectJpaRepository;
import com.inso.learning.taskflow.repository.jpa.UserJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The @Repository implementation that bridges our domain-facing
 * ProjectRepository interface to Spring Data JPA. This follows the exact
 * pattern already explained in detail inside UserRepositoryImpl: the
 * service layer depends only on the plain ProjectRepository interface, and
 * this class is the one place that talks to both ProjectJpaRepository
 * (Spring Data's generated implementation working with ProjectEntity) and
 * ProjectMapper (which converts ProjectEntity <-> domain.Project).
 *
 * Notice that create(...) and update(...) also need UserJpaRepository:
 * a Project's owner is a UserEntity relationship, so before we can save a
 * ProjectEntity we must first look up the real, managed UserEntity row for
 * the given ownerId. If that id does not exist, we throw
 * ResourceNotFoundException immediately, which the global exception
 * handler turns into a clear 404 response instead of letting a confusing
 * foreign-key error escape from Hibernate.
 */
@Repository
public class ProjectRepositoryImpl implements ProjectRepository {

    private final ProjectJpaRepository projectJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ProjectMapper projectMapper;

    public ProjectRepositoryImpl(ProjectJpaRepository projectJpaRepository,
                                  UserJpaRepository userJpaRepository,
                                  ProjectMapper projectMapper) {
        this.projectJpaRepository = projectJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.projectMapper = projectMapper;
    }

    @Override
    public Project create(Project project, Long ownerId) {
        UserEntity owner = userJpaRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + ownerId));
        ProjectEntity saved = projectJpaRepository.save(projectMapper.toEntity(project, owner));
        return projectMapper.toDomain(saved);
    }

    @Override
    public List<Project> getAll() {
        return projectJpaRepository.findAll().stream()
                .map(projectMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Project> getById(Long id) {
        return projectJpaRepository.findById(id).map(projectMapper::toDomain);
    }

    @Override
    public List<Project> getByOwnerId(Long ownerId) {
        return projectJpaRepository.findByOwnerId(ownerId).stream()
                .map(projectMapper::toDomain)
                .toList();
    }

    @Override
    public Project update(Project project) {
        ProjectEntity existing = projectJpaRepository.findById(project.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + project.getId()));
        existing.setName(project.getName());
        existing.setDescription(project.getDescription());
        return projectMapper.toDomain(projectJpaRepository.save(existing));
    }

    @Override
    public void deleteById(Long id) {
        projectJpaRepository.deleteById(id);
    }
}
