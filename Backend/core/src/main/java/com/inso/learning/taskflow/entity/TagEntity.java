package com.inso.learning.taskflow.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The persistence shape of a Tag. Note "mappedBy" is not used here because
 * TaskEntity already declared @JoinTable, making it the OWNING side of the
 * Task <-> Tag many-to-many relationship; this class is simply the INVERSE
 * side, letting us navigate from a tag back to the tasks that use it.
 */
@Entity
@Table(name = "tag")
public class TagEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "tags", fetch = FetchType.LAZY)
    private Set<TaskEntity> tasks = new HashSet<>();

    protected TagEntity() {
    }

    public TagEntity(String name) {
        this.name = name;
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

    public Set<TaskEntity> getTasks() {
        return tasks;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TagEntity that)) {
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
        return "TagEntity{id=%d, name='%s'}".formatted(id, name);
    }
}
