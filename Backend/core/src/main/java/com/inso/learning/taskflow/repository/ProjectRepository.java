package com.inso.learning.taskflow.repository;

import com.inso.learning.taskflow.domain.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    /**
     * "ownerId" is passed separately from the Project domain object here,
     * even though Project already carries a full owner User. We keep the
     * method signature explicit about which id is used to look up the
     * owner, so callers (the service layer) cannot forget to supply it -
     * see ProjectService for how the two are used together.
     */
    Project create(Project project, Long ownerId);

    List<Project> getAll();

    Optional<Project> getById(Long id);

    List<Project> getByOwnerId(Long ownerId);

    Project update(Project project);

    void deleteById(Long id);
}
