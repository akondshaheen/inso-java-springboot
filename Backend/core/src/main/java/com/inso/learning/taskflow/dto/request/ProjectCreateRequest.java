package com.inso.learning.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Notice this record no longer carries an "ownerId" field. Now that
 * Spring Security authenticates every request (see the Security stage),
 * the owner is always the currently authenticated caller - taken from
 * SecurityUtils.getCurrentUserId() in ProjectController - rather than a
 * value the client could set to any id it wants. Trusting a client-supplied
 * id for "who owns this" would let one user create resources "as" someone
 * else entirely, which is exactly the kind of bug proper authentication is
 * meant to prevent.
 */
public record ProjectCreateRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description
) {
}
