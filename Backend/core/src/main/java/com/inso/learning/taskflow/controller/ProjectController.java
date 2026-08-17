package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.domain.Project;
import com.inso.learning.taskflow.dto.request.ProjectCreateRequest;
import com.inso.learning.taskflow.dto.request.ProjectUpdateRequest;
import com.inso.learning.taskflow.dto.response.ProjectResponse;
import com.inso.learning.taskflow.security.SecurityUtils;
import com.inso.learning.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        // The owner is always the currently authenticated caller - never a
        // value taken from the request body - so a client can never create
        // a project "as" someone else.
        Project created = projectService.createProject(request.name(), request.description(), SecurityUtils.getCurrentUserId());
        return ResponseEntity.created(URI.create("/api/projects/" + created.getId()))
                .body(ProjectResponse.from(created));
    }

    @GetMapping
    public List<ProjectResponse> getAll() {
        return projectService.getAllProjects().stream().map(ProjectResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProjectResponse getById(@PathVariable Long id) {
        return ProjectResponse.from(projectService.getProjectById(id));
    }

    /**
     * QUERY PARAMETERS
     * -------------------------------------------------------------------
     * Unlike a path variable, a query parameter is OPTIONAL, extra
     * information appended to the URL after "?" (for example
     * "/api/projects?ownerId=3"). We use "required = false" here because
     * this endpoint should still work (returning every project) when no
     * ownerId is supplied at all.
     */
    @GetMapping(params = "ownerId")
    public List<ProjectResponse> getByOwner(@RequestParam(required = false) Long ownerId) {
        return projectService.getProjectsByOwner(ownerId).stream().map(ProjectResponse::from).toList();
    }

    @PutMapping("/{id}")
    public ProjectResponse update(@PathVariable Long id, @Valid @RequestBody ProjectUpdateRequest request) {
        Project updated = projectService.updateProject(id, request.name(), request.description(), SecurityUtils.getCurrentUserId());
        return ProjectResponse.from(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectService.deleteProject(id, SecurityUtils.getCurrentUserId());
        return ResponseEntity.noContent().build();
    }
}
