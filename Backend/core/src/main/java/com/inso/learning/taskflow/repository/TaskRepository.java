package com.inso.learning.taskflow.repository;

import com.inso.learning.taskflow.domain.Priority;
import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TaskRepository {

    Task create(Task task, Long projectId, Long assigneeId, Set<Long> tagIds);

    List<Task> getAll();

    Optional<Task> getById(Long id);

    /**
     * WHY DO WE NEED PAGINATION?
     * -------------------------------------------------------------------
     * Imagine a project with thousands of tasks. Returning all of them in
     * a single HTTP response would be slow, use a lot of memory, and be
     * pointless for a user who can only look at a screen full of results
     * at a time. Pageable carries the requested page number, page size,
     * and sort order together; Page<Task> carries back both the requested
     * slice of data AND metadata like the total number of elements and
     * pages, which is exactly what a "Page 3 of 12" UI needs.
     */
    Page<Task> getByProjectId(Long projectId, Pageable pageable);

    List<Task> getByAssigneeId(Long assigneeId);

    List<Task> getByStatus(TaskStatus status);

    List<Task> getUrgentUnfinishedTasks(Priority priority, TaskStatus excludedStatus);

    Task update(Task task);

    void deleteById(Long id);
}
