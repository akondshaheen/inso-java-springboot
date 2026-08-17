package com.inso.learning.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Just like ProjectCreateRequest, this record no longer carries a
 * "requestingUserId" field - now that Spring Security authenticates every
 * request, ProjectController reads the caller's id via
 * SecurityUtils.getCurrentUserId() and passes it into ProjectService's
 * authorization check itself.
 */
public record ProjectUpdateRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description
) {
}
