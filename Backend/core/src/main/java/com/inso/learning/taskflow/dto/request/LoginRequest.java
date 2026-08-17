package com.inso.learning.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * The request body for POST /api/auth/login. We deliberately keep this
 * separate from UserRegistrationRequest - a login only ever needs an
 * email and a password, not a name, and keeping DTOs narrowly focused on
 * one specific use case makes each one easier to read and validate.
 */
public record LoginRequest(

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        String email,

        @NotBlank(message = "Password must not be blank")
        String password
) {
}
