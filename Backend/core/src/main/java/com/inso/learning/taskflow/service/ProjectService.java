package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.Project;
import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.exception.AuthorizationException;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;

    public ProjectService(ProjectRepository projectRepository, UserService userService) {
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    public Project createProject(String name, String description, Long ownerId) {
        // We look the owner up first so we can build a fully-formed
        // domain.Project (with a real domain.User inside it) before
        // handing it to the repository - the service layer works entirely
        // in domain objects and never touches an entity directly.
        User owner = userService.getUserById(ownerId);
        Project project = new Project(name, description, owner);
        return projectRepository.create(project, ownerId);
    }

    public List<Project> getAllProjects() {
        return projectRepository.getAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + id));
    }

    public List<Project> getProjectsByOwner(Long ownerId) {
        return projectRepository.getByOwnerId(ownerId);
    }

    /**
     * A SIMPLE, MANUAL AUTHORIZATION CHECK
     * -------------------------------------------------------------------
     * "requestingUserId" represents whoever is currently making the
     * request. In a real, secured API (see the Security stage), this value
     * would come from the authenticated user's token rather than being
     * passed in directly - but the RULE itself (only the owner, or an
     * admin, may modify a project) is a business rule that belongs here in
     * the service layer regardless of how the caller's identity was
     * established. We introduce the rule now, in plain Java, so it is
     * easy to see exactly what it does before Spring Security is layered
     * on top of it later.
     */
    public Project updateProject(Long id, String name, String description, Long requestingUserId) {
        Project existing = getProjectById(id);
        User requester = userService.getUserById(requestingUserId);

        boolean isOwner = existing.isOwnedBy(requester);
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AuthorizationException("Only the project owner or an admin may modify this project");
        }

        existing.setName(name);
        existing.setDescription(description);
        return projectRepository.update(existing);
    }

    public void deleteProject(Long id, Long requestingUserId) {
        Project existing = getProjectById(id);
        User requester = userService.getUserById(requestingUserId);

        boolean isOwner = existing.isOwnedBy(requester);
        boolean isAdmin = requester.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new AuthorizationException("Only the project owner or an admin may delete this project");
        }

        projectRepository.deleteById(id);
    }
}
