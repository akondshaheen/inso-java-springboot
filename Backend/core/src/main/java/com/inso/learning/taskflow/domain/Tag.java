package com.inso.learning.taskflow.domain;

import java.util.Objects;

/**
 * The domain representation of a tag (for example "frontend" or "urgent").
 * A tag is a simple, small value - deliberately kept minimal.
 */
public class Tag {

    private Long id;
    private String name;

    public Tag(String name) {
        this.name = name;
    }

    public Tag(Long id, String name) {
        this.id = id;
        this.name = name;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Tag tag)) {
            return false;
        }
        return id != null && id.equals(tag.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Tag{id=%d, name='%s'}".formatted(id, name);
    }
}
