package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.Tag;
import com.inso.learning.taskflow.repository.TagRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * "Find or create" is a small, common pattern: if a tag with this name
     * already exists, reuse it; otherwise create a new one. We use
     * Optional.orElseGet(...) here (a Supplier, evaluated only if the
     * Optional is actually empty) rather than Optional.orElse(...) (which
     * would always evaluate its argument, even when not needed) - a subtle
     * but real performance difference worth knowing for interviews.
     *
     * @CacheEvict clears the "tags" cache (see getAllTags() below)
     * whenever this method runs, because it might have just created a
     * brand new tag that getAllTags()'s cached result does not know about
     * yet. It is always safer to evict a cache too often than to risk
     * serving STALE data - a cache that is occasionally slower than it
     * could be is a much smaller problem than one that silently returns
     * wrong answers.
     */
    @CacheEvict(value = "tags", allEntries = true)
    public Tag findOrCreateByName(String name) {
        return tagRepository.getByName(name)
                .orElseGet(() -> tagRepository.create(new Tag(name)));
    }

    /**
     * WHY DOES TaskCreateRequest ACCEPT tagNames AS A Set<String>, NOT A
     * List<String>?
     * -------------------------------------------------------------------
     * This is a small, real example of BIG-O THINKING influencing an API
     * design choice. If a client accidentally sent the same tag name
     * twice, a List would keep both copies, and we would do duplicate
     * work creating/looking up the same tag twice. A Set automatically
     * removes duplicates as soon as the JSON is deserialized into it, and
     * HashSet's contains()/add() operations run in O(1) average time
     * (a direct calculation of where an element belongs, via
     * hashCode()), compared to a List's contains(), which is O(n) - it
     * must check every element one by one in the worst case. For a
     * request DTO with a handful of tags this difference is invisible,
     * but choosing the right collection is a habit that matters much more
     * once a collection can hold thousands of elements.
     */
    public Set<Tag> findOrCreateAll(Set<String> names) {
        return names.stream()
                .map(this::findOrCreateByName)
                .collect(Collectors.toSet());
    }

    /**
     * WHY CACHE THIS SPECIFIC METHOD?
     * -------------------------------------------------------------------
     * The full list of tags is exactly the kind of data caching helps
     * with most: it is read far more often than it changes (many task
     * creation screens might ask "what tags exist?" to show a picker),
     * and computing it means a real round trip to the database every
     * time. @Cacheable wraps this method in a proxy that, the FIRST time
     * getAllTags() runs, calls the real method and stores its result
     * under the cache name "tags". Every subsequent call returns the
     * stored result directly, WITHOUT running the method body or
     * touching the database at all, until something evicts the cache
     * (see findOrCreateByName above).
     *
     * COMMON MISTAKE: caching data that changes constantly, or caching
     * data that is different per-user without including the user in the
     * cache key, would serve every caller the same STALE or WRONG result
     * - caching is a powerful tool specifically for data that is safe to
     * treat as "good enough for a little while".
     */
    @Cacheable("tags")
    public List<Tag> getAllTags() {
        return tagRepository.getAll();
    }
}

