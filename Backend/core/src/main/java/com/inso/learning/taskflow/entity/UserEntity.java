package com.inso.learning.taskflow.entity;

import com.inso.learning.taskflow.domain.Role;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * =============================================================================
 * STEP 5 OF OUR BUILD SEQUENCE: THE ENTITY LAYER
 * =============================================================================
 *
 * WHAT IS JPA, AND WHAT IS HIBERNATE?
 * -------------------------------------------------------------------------
 * JPA (Jakarta Persistence API) is only a SPECIFICATION - a set of
 * interfaces and annotations (@Entity, @Id, EntityManager, ...) that
 * describe HOW object-relational mapping (ORM) should work in Java. JPA
 * itself contains no working code that talks to a database.
 *
 * Hibernate is the most popular IMPLEMENTATION of that specification - the
 * actual library that reads our @Entity classes, generates SQL, opens a
 * JDBC connection, and executes queries. When we add
 * "spring-boot-starter-data-jpa" to our pom.xml, Spring Boot wires up
 * Hibernate as the JPA provider behind the scenes. Think of JPA as an
 * interface ("cook food") and Hibernate as one concrete implementation of
 * it (a specific chef).
 *
 * WHY IS THIS CLASS CALLED "UserEntity" INSTEAD OF JUST "User"?
 * -------------------------------------------------------------------------
 * We already have a plain domain.User class representing a user as a
 * business concept. This class represents the SAME real-world user, but
 * shaped for Hibernate to persist to the "app_user" table. Naming this one
 * "UserEntity" avoids a naming collision and makes it immediately clear,
 * anywhere it is imported, which "shape" of User a piece of code is
 * working with. mapper/UserMapper converts between the two.
 *
 * WHAT DOES @Entity ACTUALLY DO?
 * -------------------------------------------------------------------------
 * @Entity tells Hibernate "this class represents a row in a database
 * table". Hibernate uses Java Reflection to read this class's fields and
 * annotations at startup, and builds an internal mapping between this
 * class and the "app_user" table named in @Table.
 */
@Entity
@Table(name = "app_user")
public class UserEntity {

    /**
     * @Id marks this field as the table's primary key. @GeneratedValue
     * (strategy = IDENTITY) tells the DATABASE itself to generate the next
     * id automatically (an auto-incrementing column), instead of our Java
     * code choosing it. This is why "id" stays null until this entity is
     * actually inserted - at that point the database assigns the real
     * value and Hibernate reads it back into this field.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "final" is not used on these fields because Hibernate needs to be
    // able to set them via reflection after constructing a "blank" entity.
    // Encapsulation is still respected: every field is "private", so the
    // only way outside code can read or change them is through the public
    // getters and setters below.
    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * "mappedBy = owner" means ProjectEntity owns the foreign key column
     * (project.owner_id) - this side is only the "inverse" (read-only, from
     * Hibernate's point of view) side of a "One User has Many Projects"
     * relationship.
     *
     * FetchType.LAZY means Hibernate will NOT load a user's projects from
     * the database until this list is actually accessed in code. This
     * matters for performance: if we loaded a User just to read their
     * email, we would not want every one of their projects pulled out of
     * the database unnecessarily too.
     */
    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectEntity> ownedProjects = new ArrayList<>();

    @OneToMany(mappedBy = "assignee", fetch = FetchType.LAZY)
    private List<TaskEntity> assignedTasks = new ArrayList<>();

    /**
     * JPA REQUIRES a no-argument constructor (it can be protected). This
     * lets Hibernate create a "blank" entity via reflection before filling
     * in the fields with data read from the database. We never call this
     * constructor ourselves in normal application code - our own code
     * always goes through domain.User and UserMapper instead.
     */
    protected UserEntity() {
    }

    public UserEntity(String name, String email, String passwordHash, Role role) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
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

    public List<ProjectEntity> getOwnedProjects() {
        return ownedProjects;
    }

    public List<TaskEntity> getAssignedTasks() {
        return assignedTasks;
    }

    /**
     * We override equals()/hashCode() based on id only, and guard against a
     * null id (a brand new entity that has not been saved yet has none).
     * We use "instanceof" rather than comparing getClass() so this still
     * works correctly with Hibernate proxy objects (Hibernate sometimes
     * wraps a lazily loaded entity in a dynamically generated subclass).
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof UserEntity that)) {
            return false;
        }
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserEntity{id=%d, name='%s', email='%s', role=%s}".formatted(id, name, email, role);
    }
}
