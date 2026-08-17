package com.inso.learning.taskflow.repository.impl;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.domain.TaskStatus;
import com.inso.learning.taskflow.entity.ProjectEntity;
import com.inso.learning.taskflow.entity.TagEntity;
import com.inso.learning.taskflow.entity.TaskEntity;
import com.inso.learning.taskflow.entity.UserEntity;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.mapper.TaskMapper;
import com.inso.learning.taskflow.repository.TaskRepository;
import com.inso.learning.taskflow.repository.jpa.ProjectJpaRepository;
import com.inso.learning.taskflow.repository.jpa.TagJpaRepository;
import com.inso.learning.taskflow.repository.jpa.TaskJpaRepository;
import com.inso.learning.taskflow.repository.jpa.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The @Repository implementation bridging our domain-facing TaskRepository
 * interface to Spring Data JPA, following the same adapter pattern
 * explained in UserRepositoryImpl. TaskRepositoryImpl is the most involved
 * of the three repository implementations because a Task has the most
 * relationships to resolve before it can be saved: it must belong to an
 * existing Project, it may optionally have an assignee User, and it may
 * have zero or more Tags - create(...) below looks up all of these real,
 * managed entities first, then hands them to TaskMapper to build a single
 * fully-linked TaskEntity that Hibernate can persist in one save() call.
 */
@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final TaskJpaRepository taskJpaRepository;
    private final ProjectJpaRepository projectJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final TagJpaRepository tagJpaRepository;
    private final TaskMapper taskMapper;

    public TaskRepositoryImpl(TaskJpaRepository taskJpaRepository,
                               ProjectJpaRepository projectJpaRepository,
                               UserJpaRepository userJpaRepository,
                               TagJpaRepository tagJpaRepository,
                               TaskMapper taskMapper) {
        this.taskJpaRepository = taskJpaRepository;
        this.projectJpaRepository = projectJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.tagJpaRepository = tagJpaRepository;
        this.taskMapper = taskMapper;
    }

    @Override
    public Task create(Task task, Long projectId, Long assigneeId, Set<Long> tagIds) {
        ProjectEntity project = projectJpaRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id " + projectId));

        // The assignee is optional, so we only look one up if an id was
        // actually supplied - this is exactly the kind of "might or might
        // not be there" situation java.util.Optional was designed for.
        UserEntity assignee = assigneeId == null
                ? null
                : userJpaRepository.findById(assigneeId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + assigneeId));

        Set<TagEntity> tags = tagIds == null
                ? new HashSet<>()
                : new HashSet<>(tagJpaRepository.findAllById(tagIds));

        TaskEntity saved = taskJpaRepository.save(taskMapper.toEntity(task, project, assignee, tags));
        return taskMapper.toDomain(saved);
    }

    /**
     * Calls the overridden findAll() in TaskJpaRepository (annotated with
     * @EntityGraph there) instead of relying on plain, un-annotated
     * inheritance, specifically to avoid the N+1 query problem:
     * TaskMapper.toDomain(...) below calls entity.getProject() and
     * entity.getAssignee() for every task, which would otherwise trigger
     * one extra lazy-loading query per task.
     */
    @Override
    public List<Task> getAll() {
        return taskJpaRepository.findAll().stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Task> getById(Long id) {
        return taskJpaRepository.findById(id).map(taskMapper::toDomain);
    }

    @Override
    public Page<Task> getByProjectId(Long projectId, Pageable pageable) {
        // Page.map(...) converts every TaskEntity in the page into a
        // domain Task while keeping the same pagination metadata (total
        // elements, total pages) intact - it works just like
        // Optional.map(), but for a whole page of results at once.
        return taskJpaRepository.findByProjectId(projectId, pageable).map(taskMapper::toDomain);
    }

    @Override
    public List<Task> getByAssigneeId(Long assigneeId) {
        return taskJpaRepository.findByAssigneeId(assigneeId).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public List<Task> getByStatus(TaskStatus status) {
        return taskJpaRepository.findByStatus(status).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    @Override
    public List<Task> getUrgentUnfinishedTasks(Priority priority, TaskStatus excludedStatus) {
        return taskJpaRepository.findUrgentUnfinishedTasks(priority, excludedStatus).stream()
                .map(taskMapper::toDomain)
                .toList();
    }

    /**
     * Updates only the simple, descriptive fields of a task (title,
     * description, status, priority, due date). Changing WHICH project a
     * task belongs to, WHO is assigned, or WHICH tags it has are treated
     * as separate, more significant operations - kept as their own
     * dedicated service methods (see TaskService) rather than silently
     * folded into a generic update, so each operation's intent stays clear
     * and easy to reason about.
     */
    @Override
    public Task update(Task task) {
        TaskEntity existing = taskJpaRepository.findById(task.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + task.getId()));
        existing.setTitle(task.getTitle());
        existing.setDescription(task.getDescription());
        existing.setStatus(task.getStatus());
        existing.setPriority(task.getPriority());
        existing.setDueDate(task.getDueDate());
        return taskMapper.toDomain(taskJpaRepository.save(existing));
    }

    @Override
    public void deleteById(Long id) {
        taskJpaRepository.deleteById(id);
    }
}
