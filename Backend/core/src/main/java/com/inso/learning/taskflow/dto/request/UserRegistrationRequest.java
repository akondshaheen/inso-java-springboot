package com.inso.learning.taskflow.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * =============================================================================
 * STEP 11 OF OUR BUILD SEQUENCE: REQUEST DTOs AND VALIDATION
 * =============================================================================
 *
 * WHY DO WE USE A SEPARATE "DTO" INSTEAD OF ACCEPTING domain.User DIRECTLY?
 * -------------------------------------------------------------------------
 * A DTO (Data Transfer Object) describes exactly the shape of data crossing
 * the HTTP boundary - nothing more, nothing less. This request, for
 * example, deliberately has no "id", "role" or "createdAt" field: a client
 * signing up should never be able to choose their own id, promote
 * themselves to ADMIN, or fake a creation timestamp. If our controller
 * accepted a domain.User (or worse, a UserEntity) directly from the
 * request body, a client could set ANY field, including ones that should
 * only ever be controlled by our own server-side logic. The DTO's shape
 * is our security boundary for what a client is allowed to submit.
 *
 * WHY A "record" INSTEAD OF A REGULAR CLASS?
 * -------------------------------------------------------------------------
 * A Java "record" (introduced in Java 16) is a compact way to declare an
 * immutable data carrier class. Writing
 *   public record UserRegistrationRequest(String name, String email, String password) {}
 * automatically generates: private final fields, a canonical constructor,
 * getter-style accessor methods (name(), email(), password() - no "get"
 * prefix), plus working equals(), hashCode() and toString() based on all
 * the fields. This removes a large amount of repetitive boilerplate for a
 * class whose only job is to carry a fixed bundle of data - exactly what a
 * request or response DTO is.
 *
 * WHAT DO THESE VALIDATION ANNOTATIONS DO, AND HOW DOES @Valid CONNECT
 * THEM TO AN HTTP REQUEST?
 * -------------------------------------------------------------------------
 * @NotBlank, @Size and @Email are Bean Validation constraints (from the
 * "jakarta.validation" package, provided by our
 * spring-boot-starter-validation dependency). On their own they do
 * nothing - they are just metadata sitting on this class. The actual
 * checking happens when a controller method parameter of this type is
 * annotated with @Valid (see UserController): Spring MVC then asks the
 * Bean Validation provider (Hibernate Validator) to check every annotated
 * field on the deserialized object BEFORE the controller method's body
 * runs. If any constraint fails, Spring throws a
 * MethodArgumentNotValidException instead of calling our controller method
 * at all - our global exception handler (built in a later stage) catches
 * that exception and turns it into a clean, consistent 400 Bad Request
 * response listing exactly which fields failed and why.
 */
public record UserRegistrationRequest(

        @NotBlank(message = "Name must not be blank")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email must be a valid email address")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String email,

        @NotBlank(message = "Password must not be blank")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password
) {
}
