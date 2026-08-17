package com.inso.learning.taskflow.dto.response;

import com.inso.learning.taskflow.domain.Role;
import com.inso.learning.taskflow.domain.User;

import java.time.LocalDateTime;

/**
 * RESPONSE DTOs: THE OTHER HALF OF THE BOUNDARY
 * -------------------------------------------------------------------------
 * Just as request DTOs control what a client is ALLOWED TO SEND us,
 * response DTOs control what we are willing to SEND BACK. Notice this
 * class has no "passwordHash" field at all - even though domain.User has
 * one. If a controller accidentally returned a domain.User directly from
 * an endpoint, Jackson (the JSON library) would serialize every public
 * getter it can find, including getPasswordHash() - leaking a password
 * hash straight into an HTTP response. Explicitly building a UserResponse
 * makes it impossible to leak a field like that by accident, because this
 * class simply does not have one to serialize.
 *
 * The static "from(...)" method is a small, conventional pattern: it
 * keeps the conversion logic from domain.User to this DTO in one place,
 * right next to the DTO's own definition, so the controller only needs to
 * call "UserResponse.from(user)" instead of repeating the same field-by-
 * field copying everywhere a UserResponse is built.
 */
public record UserResponse(
        Long id,
        String name,
        String email,
        Role role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
