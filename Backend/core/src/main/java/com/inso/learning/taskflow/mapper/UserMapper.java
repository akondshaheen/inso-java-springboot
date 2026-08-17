package com.inso.learning.taskflow.mapper;

import com.inso.learning.taskflow.domain.User;
import com.inso.learning.taskflow.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * =============================================================================
 * STEP 6 OF OUR BUILD SEQUENCE: THE ENTITY <-> DOMAIN MAPPER
 * =============================================================================
 *
 * WHAT DOES A MAPPER DO, AND WHY DO WE NEED ONE?
 * -------------------------------------------------------------------------
 * We now have two different classes describing "a user": domain.User (a
 * plain business object) and entity.UserEntity (the JPA-annotated
 * persistence object). Something has to be responsible for converting one
 * into the other, in both directions - that is this class's only job. By
 * putting that conversion logic in ONE dedicated place, no other class
 * (service, repository implementation, controller) needs to know both
 * shapes exist; they can just ask this mapper to do the translation.
 *
 * WHY IS THIS A SPRING @Component?
 * -------------------------------------------------------------------------
 * Marking this class @Component tells Spring's component scan to create
 * exactly one instance of it (a "bean") and make it available for
 * Dependency Injection. Our UserRepositoryImpl (created in the next step)
 * simply declares "I need a UserMapper" in its constructor, and Spring
 * supplies this same shared instance automatically - we never write "new
 * UserMapper()" ourselves anywhere in the application.
 */
@Component
public class UserMapper {

    /**
     * Converts a persistence object into a business object. Used every
     * time we read a user back out of the database and need to hand it up
     * to the service layer.
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.getCreatedAt()
        );
    }

    /**
     * Converts a business object into a persistence object, ready to be
     * saved by Hibernate. We do not copy "id" onto a brand new
     * UserEntity here for a fresh signup, because the database itself
     * generates it (see @GeneratedValue on UserEntity.id) - Hibernate
     * simply ignores a null id and inserts a new row.
     */
    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        return new UserEntity(domain.getName(), domain.getEmail(), domain.getPasswordHash(), domain.getRole());
    }
}
