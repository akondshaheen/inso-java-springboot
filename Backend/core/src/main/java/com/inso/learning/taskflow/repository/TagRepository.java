package com.inso.learning.taskflow.repository;

import com.inso.learning.taskflow.domain.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    Tag create(Tag tag);

    List<Tag> getAll();

    Optional<Tag> getById(Long id);

    Optional<Tag> getByName(String name);
}
