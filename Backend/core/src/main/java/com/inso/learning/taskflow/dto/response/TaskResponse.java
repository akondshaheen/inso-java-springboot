package com.inso.learning.taskflow.dto.response;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.Tag;
import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.domain.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A RESPONSE DTO representing a Task the way we want the client to see it
 * in JSON, returned by TaskController's endpoints. Just like ProjectResponse
 * and UserResponse, this record exists to control exactly what shape of
 * data leaves our API - it is built from the domain Task by the static
 * from(...) factory method below, never returned directly as the domain
 * object itself.
 *
 * A few shaping decisions are worth understanding:
 *   - "projectId" and "projectName" are flattened out of the full Project,
 *     instead of nesting an entire ProjectResponse inside every Task. This
 *     keeps the JSON small and avoids ever needing to serialize a Task's
 *     Project's own list of Tasks (which would risk infinite recursion).
 *   - "assignee" reuses UserResponse.from(...) but only when an assignee
 *     actually exists; task.getAssignee() can be null because assignment
 *     is optional, so we guard for null here instead of letting a
 *     NullPointerException happen inside UserResponse.from(...).
 *   - "tags" DEMONSTRATES THE STREAM API: task.getTags() is a
 *     Set<Tag> (domain objects with an id and a name); stream() opens a
 *     pipeline over that set, .map(Tag::getName) is a METHOD REFERENCE
 *     that transforms each Tag into just its String name, and
 *     .collect(Collectors.toSet()) gathers the results back into a new
 *     Set<String> - exactly the JSON-friendly shape the client needs,
 *     without a manual for-loop and a temporary mutable collection.
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Priority priority,
        LocalDate dueDate,
        Long projectId,
        String projectName,
        UserResponse assignee,
        Set<String> tags,
        LocalDateTime createdAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getDueDate(),
                task.getProject().getId(),
                task.getProject().getName(),
                task.getAssignee() == null ? null : UserResponse.from(task.getAssignee()),
                task.getTags().stream().map(Tag::getName).collect(Collectors.toSet()),
                task.getCreatedAt()
        );
    }
}
