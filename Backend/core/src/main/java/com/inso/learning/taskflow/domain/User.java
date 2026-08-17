package com.inso.learning.taskflow.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The domain representation of a user. This is a plain Java class (a
 * "POJO" - Plain Old Java Object) with no framework annotations at all.
 * Our service layer, and the interfaces our repositories expose, work with
 * THIS class - not with the JPA-annotated UserEntity in the entity/
 * package. The mapper/UserMapper class is the only place that knows how to
 * convert between the two.
 *
 * ENCAPSULATION IN PRACTICE
 * -------------------------------------------------------------------------
 * Every field here is "private". No other class can reach in and change a
 * User's email directly - any change must go through setEmail(...), which
 * gives us one single place to add validation later (for example,
 * rejecting an empty email) without needing to change every class that
 * uses User. This is what "encapsulation" means in practice: hiding an
 * object's internal state and only exposing it through controlled methods.
 */
public class User {

    // "id" is null for a User that has not been saved yet - the repository
    // layer fills this in once the database has generated a real id.
    private Long id;
    private String name;
    private String email;

    // Never the plain-text password - always a bcrypt hash (see the
    // security stage). We keep it out of toString() so it can never be
    // accidentally printed into application logs.
    private String passwordHash;
    private Role role;
    private final LocalDateTime createdAt;

    /**
     * This constructor is used by OUR code to create a brand new User in
     * memory, before it has been saved. "id" is intentionally not a
     * parameter - the database decides it once the row is inserted.
     */
    public User(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * This second constructor is used by UserMapper to rebuild a domain
     * User from data that already exists in the database (so it already
     * has a real id and a known createdAt timestamp).
     *
     * METHOD OVERLOADING: this is our second constructor with a different
     * parameter list (same class, same constructor name implicitly, but a
     * different signature). The compiler chooses which one to call based
     * on the number and types of arguments supplied at the call site.
     */
    public User(Long id, String name, String email, String passwordHash, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * WHY OVERRIDE equals() AND hashCode() TOGETHER?
     * -------------------------------------------------------------------
     * By default, Object.equals() compares references (are these the exact
     * same object in memory?), which is rarely what we want when comparing
     * two User objects representing the same real user. We override
     * equals() so two Users are considered equal when they share the same
     * id. Java's contract REQUIRES that equal objects return the same
     * hashCode() - collections like HashSet/HashMap use hashCode() first to
     * find the right "bucket", and only then use equals() to confirm an
     * exact match. Overriding one without the other is a common, subtle
     * bug: an object can fail to be found in a HashSet even though an
     * "equal" object was added to it earlier.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User user)) {
            return false;
        }
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{id=%d, name='%s', email='%s', role=%s}".formatted(id, name, email, role);
    }
}
