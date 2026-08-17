package com.inso.learning.taskflow.dto.request;

import com.inso.learning.taskflow.domain.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Used with a full PUT update (replacing every editable field on a task).
 * See TaskStatusUpdateRequest for the smaller PATCH-style alternative that
 * only changes the status - the difference between the two is explained
 * on the controller's PUT and PATCH endpoints.
 */
public record TaskUpdateRequest(

        @NotBlank(message = "Title must not be blank")
        @Size(max = 150, message = "Title must be at most 150 characters")
        String title,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,

        @NotNull(message = "Priority must be provided")
        Priority priority,

        LocalDate dueDate
) {
}
