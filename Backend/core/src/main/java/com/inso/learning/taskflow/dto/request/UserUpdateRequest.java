package com.inso.learning.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A REQUEST DTO used when a user updates their own profile through
 * PUT /api/users/{id}. It intentionally only contains "name" and "email" -
 * fields the API allows a user to change about themselves. Notice what is
 * NOT here: no id (that comes from the URL path variable), no password
 * (password changes deserve their own dedicated endpoint and validation
 * rules, not a general profile update), and no role (a normal user should
 * never be able to promote themselves to ADMIN by sending extra JSON
 * fields - this is a simple but important security habit called
 * "allow-listing" fields instead of trusting whatever the client sends).
 *
 * Like every request DTO in this project, the validation annotations
 * (@NotBlank, @Size, @Email) only take effect when the controller method's
 * parameter is annotated with @Valid; Spring then runs these checks before
 * UserService is ever called, and any failure becomes a 400 Bad Request
 * with a clear list of validation errors instead of a confusing failure
 * deeper in the service or database layer.
 */
public record UserUpdateRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email
) {
}
