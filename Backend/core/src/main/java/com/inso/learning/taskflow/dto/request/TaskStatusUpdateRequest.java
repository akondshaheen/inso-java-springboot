package com.inso.learning.taskflow.dto.request;

import com.inso.learning.taskflow.domain.TaskStatus;
import jakarta.validation.constraints.NotNull;

/**
 * PUT VS PATCH
 * -------------------------------------------------------------------------
 * PUT is used to REPLACE a whole resource: the client sends every editable
 * field, and any field left out is understood as "clear this field" (or is
 * simply not supported, as with TaskUpdateRequest above, which requires
 * every field). PATCH is used for a PARTIAL update: the client sends only
 * the specific field(s) they want to change. This DTO exists specifically
 * for a PATCH endpoint that changes only a task's status, without forcing
 * the client to resend the title, description, priority and due date just
 * to move a task from TODO to IN_PROGRESS.
 */
public record TaskStatusUpdateRequest(

        @NotNull(message = "Status must be provided")
        TaskStatus status
) {
}
