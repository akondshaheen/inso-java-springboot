package com.inso.learning.taskflow.repository.impl;

import com.inso.learning.taskflow.domain.Tag;
import com.inso.learning.taskflow.entity.TagEntity;
import com.inso.learning.taskflow.mapper.TagMapper;
import com.inso.learning.taskflow.repository.TagRepository;
import com.inso.learning.taskflow.repository.jpa.TagJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The @Repository implementation bridging our domain-facing TagRepository
 * interface to Spring Data JPA, following the same adapter pattern
 * explained in UserRepositoryImpl and ProjectRepositoryImpl. TagRepositoryImpl
 * is the simplest of the three repository implementations in this project
 * because Tag has no relationships that need extra lookups before saving -
 * every method here is a short "call the JPA repository, then convert the
 * result with TagMapper" one-liner.
 */
@Repository
public class TagRepositoryImpl implements TagRepository {

    private final TagJpaRepository tagJpaRepository;
    private final TagMapper tagMapper;

    public TagRepositoryImpl(TagJpaRepository tagJpaRepository, TagMapper tagMapper) {
        this.tagJpaRepository = tagJpaRepository;
        this.tagMapper = tagMapper;
    }

    @Override
    public Tag create(Tag tag) {
        TagEntity saved = tagJpaRepository.save(tagMapper.toEntity(tag));
        return tagMapper.toDomain(saved);
    }

    @Override
    public List<Tag> getAll() {
        return tagJpaRepository.findAll().stream()
                .map(tagMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Tag> getById(Long id) {
        return tagJpaRepository.findById(id).map(tagMapper::toDomain);
    }

    @Override
    public Optional<Tag> getByName(String name) {
        return tagJpaRepository.findByName(name).map(tagMapper::toDomain);
    }
}
