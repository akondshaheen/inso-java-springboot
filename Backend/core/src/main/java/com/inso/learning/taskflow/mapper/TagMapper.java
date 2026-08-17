package com.inso.learning.taskflow.mapper;

import com.inso.learning.taskflow.domain.Tag;
import com.inso.learning.taskflow.entity.TagEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between the domain.Tag class (plain business object, no
 * framework annotations) and the entity.TagEntity class (the JPA-annotated
 * shape Hibernate persists to the "tag" table). This follows exactly the
 * same reasoning already explained in UserMapper: keeping the conversion
 * logic in one small, dedicated, @Component-annotated class means the
 * service layer only ever needs to work with domain.Tag, and the
 * repository layer (TagRepositoryImpl) is the only place that touches
 * both shapes.
 *
 * Tag is the simplest mapper in this project because Tag has no
 * relationships of its own to convert (it only has an id and a name) -
 * compare this to TaskMapper and ProjectMapper, which also need to
 * convert related entities like Project, User, and other Tags.
 */
@Component
public class TagMapper {

    public Tag toDomain(TagEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Tag(entity.getId(), entity.getName());
    }

    public TagEntity toEntity(Tag domain) {
        if (domain == null) {
            return null;
        }
        return new TagEntity(domain.getName());
    }
}
