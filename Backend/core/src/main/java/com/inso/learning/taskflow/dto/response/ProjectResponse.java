package com.inso.learning.taskflow.dto.response;

import com.inso.learning.taskflow.domain.Project;

import java.time.LocalDateTime;

/**
 * A RESPONSE DTO representing a Project as returned to API clients. It
 * follows the same pattern already explained in UserResponse: a static
 * from(...) factory method converts our internal domain.Project into this
 * record, so ProjectController never returns the domain object directly.
 *
 * Notice "owner" is a full nested UserResponse rather than just an
 * ownerId. This is a deliberate API design choice: it lets a client
 * display the owner's name and email directly from a single GET
 * /api/projects/{id} call, without a second round trip to
 * GET /api/users/{ownerId}. The trade-off is a slightly larger JSON
 * payload - a reasonable one here because a Project always has exactly
 * one owner, so there is no risk of an ever-growing nested list like the
 * one we avoid inside TaskResponse (a Task's Project does not also list
 * every other Task in that project).
 */
public record ProjectResponse(
        Long id,
        String name,
        String description,
        UserResponse owner,
        LocalDateTime createdAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                UserResponse.from(project.getOwner()),
                project.getCreatedAt()
        );
    }
}
