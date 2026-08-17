package com.inso.learning.taskflow.entity;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.TaskStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The persistence shape of a Task. A Task belongs to exactly one
 * ProjectEntity, can optionally be assigned to one UserEntity, and can have
 * many TagEntity rows through a join table (a classic @ManyToMany
 * relationship). Notice this entity reuses the domain.Priority and
 * domain.TaskStatus enums directly - a fixed set of values like this is a
 * genuine shared business concept, so duplicating the enum in both layers
 * would add no real benefit, only extra mapping code.
 */
@Entity
@Table(name = "task", indexes = {
        // These three columns are exactly the ones our query methods
        // filter on (getTasksForProject, getTasksForAssignee, getByStatus
        // in TaskService/TaskRepository) - indexing them keeps those
        // lookups fast even as the "task" table grows to hold thousands
        // of rows, instead of degrading into a slow full-table scan.
        @Index(name = "idx_task_project_id", columnList = "project_id"),
        @Index(name = "idx_task_assignee_id", columnList = "assignee_id"),
        @Index(name = "idx_task_status", columnList = "status")
})
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Priority priority;

    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    /**
     * "nullable = true" (the default) because a Task can exist before
     * anyone is assigned to work on it - assignment is optional.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    /**
     * @ManyToMany needs an extra database table (a "join table") because,
     * unlike @ManyToOne, neither side can hold a single simple foreign key
     * - one Task can have many Tags AND one Tag can be used by many Tasks.
     * @JoinTable tells Hibernate to create/use a "task_tags" table with two
     * columns (task_id, tag_id) to store every Task-Tag pairing. This is
     * the OWNING side of the relationship (it declares @JoinTable), so
     * changes made to this "tags" set are what Hibernate writes to the
     * join table.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<TagEntity> tags = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected TaskEntity() {
    }

    public TaskEntity(String title, String description, Priority priority, LocalDate dueDate, ProjectEntity project) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public ProjectEntity getProject() {
        return project;
    }

    public void setProject(ProjectEntity project) {
        this.project = project;
    }

    public UserEntity getAssignee() {
        return assignee;
    }

    public void setAssignee(UserEntity assignee) {
        this.assignee = assignee;
    }

    public Set<TagEntity> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaskEntity that)) {
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
        return "TaskEntity{id=%d, title='%s', status=%s, priority=%s}".formatted(id, title, status, priority);
    }
}
