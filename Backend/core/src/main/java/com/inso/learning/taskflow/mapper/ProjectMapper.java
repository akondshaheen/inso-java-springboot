package com.inso.learning.taskflow.mapper;

import com.inso.learning.taskflow.domain.Project;
import com.inso.learning.taskflow.entity.ProjectEntity;
import com.inso.learning.taskflow.entity.UserEntity;
import org.springframework.stereotype.Component;

/**
 * Converts between domain.Project and entity.ProjectEntity. Notice
 * toEntity(...) below takes the OWNER's UserEntity as a separate
 * parameter, instead of trying to build it itself from
 * "domain.getOwner()". This is deliberate: by the time ProjectRepositoryImpl
 * calls this method, it has already looked up the real, already-persisted
 * UserEntity through UserJpaRepository. Re-deriving a brand new UserEntity
 * from the domain object here instead would risk creating a second,
 * duplicate user row rather than linking to the existing one.
 */
@Component
public class ProjectMapper {

    private final UserMapper userMapper;

    /**
     * CONSTRUCTOR INJECTION: ProjectMapper declares that it needs a
     * UserMapper to do its job. Spring sees this constructor at startup,
     * finds the single UserMapper bean it already created, and passes it
     * in automatically when it creates this ProjectMapper bean. We never
     * call "new UserMapper()" here ourselves - Spring's Dependency
     * Injection wires it up for us.
     */
    public ProjectMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public Project toDomain(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }
        return new Project(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                userMapper.toDomain(entity.getOwner()),
                entity.getCreatedAt()
        );
    }

    public ProjectEntity toEntity(Project domain, UserEntity ownerEntity) {
        if (domain == null) {
            return null;
        }
        return new ProjectEntity(domain.getName(), domain.getDescription(), ownerEntity);
    }
}
