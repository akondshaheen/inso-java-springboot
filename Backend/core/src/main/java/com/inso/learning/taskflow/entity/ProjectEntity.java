package com.inso.learning.taskflow.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The persistence shape of a Project. This class demonstrates both sides
 * of a relationship at once: it is the OWNING side of a @ManyToOne to
 * UserEntity (it holds the "owner_id" foreign key), and the INVERSE side of
 * a @OneToMany to TaskEntity.
 *
 * WHY DOES @Table(indexes = ...) MATTER FOR PERFORMANCE?
 * -------------------------------------------------------------------------
 * Without an index, the database must scan every single row of the
 * "project" table to answer a query like "find all projects where
 * owner_id = 5" - an O(n) operation that gets slower as the table grows.
 * A DATABASE INDEX is a separate, sorted data structure (commonly a
 * B-tree) that lets the database jump almost directly to the matching
 * rows instead, closer to O(log n). We index "owner_id" here because
 * ProjectRepository.getByOwnerId(...) (used by ProjectJpaRepository's
 * findByOwnerId) filters on exactly this column - indexing a column that
 * is rarely searched on would waste storage and slightly slow down writes
 * (every insert/update also has to update the index) for no real benefit.
 */
@Entity
@Table(name = "project", indexes = {
        @Index(name = "idx_project_owner_id", columnList = "owner_id")
})
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 1000)
    private String description;

    /**
     * "Many Projects belong to One User" - this is the OWNING side of the
     * relationship, because this entity's table holds the foreign key
     * column ("owner_id"). Whatever we set here is what Hibernate actually
     * writes to the database.
     *
     * FetchType.LAZY means the owning UserEntity is only loaded from the
     * database the moment code calls "project.getOwner()...". Without
     * this, simply loading a list of Projects would trigger a separate
     * query for every single owner - a preview of the "N+1 query problem"
     * we look at closely in the performance stage.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

    /**
     * "One Project has Many Tasks". "mappedBy = project" means TaskEntity
     * owns the foreign key (task.project_id); this list is the read-only,
     * "inverse" side from Hibernate's point of view - Hibernate only looks
     * at the Task side when deciding what to write to the database.
     *
     * CascadeType.ALL means an operation performed on a Project (for
     * example deleting it) is automatically cascaded to its Tasks too, so
     * we do not end up with orphaned Task rows pointing at a Project that
     * no longer exists. orphanRemoval = true additionally means that if a
     * Task is removed from this list in Java code, Hibernate deletes that
     * Task row from the database as well.
     */
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskEntity> tasks = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected ProjectEntity() {
    }

    public ProjectEntity(String name, String description, UserEntity owner) {
        this.name = name;
        this.description = description;
        this.owner = owner;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public List<TaskEntity> getTasks() {
        return tasks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProjectEntity that)) {
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
        return "ProjectEntity{id=%d, name='%s'}".formatted(id, name);
    }
}
