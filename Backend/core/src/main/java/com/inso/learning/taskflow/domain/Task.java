package com.inso.learning.taskflow.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The domain representation of a task. This class implements
 * Comparable<Task>, which gives Task a single "natural ordering" (by due
 * date, soonest first). Later, in the service layer, we also build
 * separate Comparator objects to sort by priority or title instead -
 * Comparable defines the ONE default way to sort a class, while a
 * Comparator lets any piece of code define as many alternative orderings
 * as it needs, without ever changing the Task class itself.
 */
public class Task implements Comparable<Task> {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private Project project;

    // Optional: a task can exist before anyone is assigned to work on it.
    // Because this is genuinely optional, service-layer code that READS
    // this field wraps it in java.util.Optional instead of scattering
    // manual null checks everywhere (see TaskService).
    private User assignee;

    private final Set<Tag> tags = new HashSet<>();
    private final LocalDateTime createdAt;

    public Task(String title, String description, Priority priority, LocalDate dueDate, Project project) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.TODO; // Every new task naturally starts as "not started yet".
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.createdAt = LocalDateTime.now();
    }

    public Task(Long id, String title, String description, TaskStatus status, Priority priority,
                LocalDate dueDate, Project project, User assignee, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.project = project;
        this.assignee = assignee;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Implementing Comparable requires this single method. It must return
     * a negative number if "this" belongs before "other", zero if they are
     * equal for ordering purposes, and a positive number if "this" belongs
     * after "other". Collections.sort(...) and stream().sorted() both use
     * this method automatically whenever no separate Comparator is
     * supplied. We treat a null dueDate as "furthest in the future"
     * (nullsLast) so tasks without a deadline sink to the end of a sorted
     * list instead of causing a NullPointerException.
     */
    @Override
    public int compareTo(Task other) {
        return Comparator.comparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .compare(this, other);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Task task)) {
            return false;
        }
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{id=%d, title='%s', status=%s, priority=%s}".formatted(id, title, status, priority);
    }
}
