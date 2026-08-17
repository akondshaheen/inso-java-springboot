package com.inso.learning.taskflow.mapper;

import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.entity.ProjectEntity;
import com.inso.learning.taskflow.entity.TagEntity;
import com.inso.learning.taskflow.entity.TaskEntity;
import com.inso.learning.taskflow.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts between domain.Task and entity.TaskEntity. Just like
 * ProjectMapper, toEntity(...) accepts already-resolved related entities
 * (project, assignee, tags) as parameters rather than trying to rebuild
 * them from the domain object, because the repository implementation
 * layer is responsible for looking those up first.
 */
@Component
public class TaskMapper {

    private final ProjectMapper projectMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;

    public TaskMapper(ProjectMapper projectMapper, UserMapper userMapper, TagMapper tagMapper) {
        this.projectMapper = projectMapper;
        this.userMapper = userMapper;
        this.tagMapper = tagMapper;
    }

    public Task toDomain(TaskEntity entity) {
        if (entity == null) {
            return null;
        }
        Task task = new Task(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getStatus(),
                entity.getPriority(),
                entity.getDueDate(),
                projectMapper.toDomain(entity.getProject()),
                // The assignee is optional - passing a null UserEntity
                // through userMapper.toDomain(...) simply returns null,
                // which is exactly what we want here.
                userMapper.toDomain(entity.getAssignee()),
                entity.getCreatedAt()
        );
        // Using the Stream API's map() to convert every TagEntity in the
        // set into a domain Tag, then collect(...) to gather the results
        // back into a new Set - a natural, common use of streams for
        // transforming one collection into another of a different type.
        task.getTags().addAll(entity.getTags().stream()
                .map(tagMapper::toDomain)
                .collect(Collectors.toSet()));
        return task;
    }

    public TaskEntity toEntity(Task domain, ProjectEntity projectEntity, UserEntity assigneeEntity, Set<TagEntity> tagEntities) {
        if (domain == null) {
            return null;
        }
        TaskEntity entity = new TaskEntity(domain.getTitle(), domain.getDescription(), domain.getPriority(),
                domain.getDueDate(), projectEntity);
        entity.setStatus(domain.getStatus());
        entity.setAssignee(assigneeEntity);
        entity.getTags().addAll(tagEntities);
        return entity;
    }
}
