package com.inso.learning.taskflow.dto.request;

import com.inso.learning.taskflow.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * A REQUEST DTO for creating a new Task through POST /api/tasks.
 * -------------------------------------------------------------------------
 * Just like UserRegistrationRequest, this is a plain Java record that
 * exists only to describe the shape of the JSON body the client sends -
 * it is never stored in the database and never passed directly into the
 * domain Task class. Instead, TaskController reads this DTO, and
 * TaskService.createTask(...) uses its individual fields to build a real
 * Task together with the Project, assignee User, and Tags it belongs to.
 * Keeping a separate request DTO for each operation (create, update,
 * status-change) means each one can carry exactly the fields that
 * operation needs and nothing more - see TaskUpdateRequest and
 * TaskStatusUpdateRequest for the other two shapes used by this project.
 *
 * The @NotBlank, @Size, and @NotNull annotations are Bean Validation rules.
 * When a controller method's parameter is marked @Valid, Spring runs these
 * checks automatically before the method body executes; if any rule
 * fails, a MethodArgumentNotValidException is thrown and caught by
 * GlobalExceptionHandler, which turns it into a 400 Bad Request response
 * listing every field that failed - the caller never even reaches
 * TaskService with bad data.
 *
 * "assigneeId" and "tagNames" are deliberately allowed to be null/empty:
 * a task can be created before anyone is assigned to it, and tags are an
 * optional way to label a task. "projectId" is required, however, because
 * every task must belong to exactly one project - see the @ManyToOne
 * relationship on TaskEntity.project.
 */
public record TaskCreateRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Priority must be provided")
        Priority priority,

        LocalDate dueDate,

        @NotNull(message = "projectId must be provided")
        Long projectId,

        // Nullable on purpose: assigning a task to someone is optional at
        // creation time.
        Long assigneeId,

        Set<String> tagNames
) {
}
