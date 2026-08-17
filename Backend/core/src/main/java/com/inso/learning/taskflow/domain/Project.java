package com.inso.learning.taskflow.domain;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The domain representation of a project. A Project belongs to exactly one
 * User (its owner). We keep a full User object here (rather than just an
 * ownerId) because the service layer frequently needs the owner's details
 * (for example, to check "is the current user allowed to edit this
 * project?" during authorization) - passing a whole domain User avoids an
 * extra database lookup every time that check happens.
 */
public class Project {

    private Long id;
    private String name;
    private String description;
    private User owner;
    private final LocalDateTime createdAt;

    public Project(String name, String description, User owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
    }

    public Project(Long id, String name, String description, User owner, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.owner = owner;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * A small piece of BUSINESS LOGIC that belongs on the domain object
     * itself rather than scattered through the service layer: only the
     * owner (or an admin, checked separately) is allowed to modify a
     * project. Keeping a rule like this next to the data it reasons about
     * is a simple example of encapsulation protecting business invariants,
     * not just field values.
     */
    public boolean isOwnedBy(User candidate) {
        return owner != null && candidate != null && owner.equals(candidate);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Project project)) {
            return false;
        }
        return id != null && id.equals(project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{id=%d, name='%s'}".formatted(id, name);
    }
}
