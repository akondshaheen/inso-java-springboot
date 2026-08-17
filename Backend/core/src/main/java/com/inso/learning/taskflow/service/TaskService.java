package com.inso.learning.taskflow.service;

import com.inso.learning.taskflow.domain.*;
import com.inso.learning.taskflow.exception.ResourceNotFoundException;
import com.inso.learning.taskflow.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;
    private final TagService tagService;

    public TaskService(TaskRepository taskRepository, ProjectService projectService, TagService tagService) {
        this.taskRepository = taskRepository;
        this.projectService = projectService;
        this.tagService = tagService;
    }

    public Task createTask(String title, String description, Priority priority, LocalDate dueDate,
                            Long projectId, Long assigneeId, Set<String> tagNames) {
        // Confirm the project actually exists before creating a task for
        // it - getProjectById throws ResourceNotFoundException otherwise,
        // giving the caller a clear 404 instead of a confusing database
        // error deeper in the stack.
        Project project = projectService.getProjectById(projectId);
        Task task = new Task(title, description, priority, dueDate, project);

        Set<Long> tagIds = tagNames == null || tagNames.isEmpty()
                ? Set.of()
                : tagService.findOrCreateAll(tagNames).stream()
                        .map(Tag::getId)
                        .collect(Collectors.toSet());

        return taskRepository.create(task, projectId, assigneeId, tagIds);
    }

    public List<Task> getAllTasks() {
        return taskRepository.getAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id " + id));
    }

    public Page<Task> getTasksForProject(Long projectId, Pageable pageable) {
        return taskRepository.getByProjectId(projectId, pageable);
    }

    public List<Task> getTasksForAssignee(Long assigneeId) {
        return taskRepository.getByAssigneeId(assigneeId);
    }

    /**
     * DEMONSTRATES THE STREAM API: filter(), then sorted() with the class's
     * natural Comparable ordering (by due date, see Task.compareTo).
     * -------------------------------------------------------------------
     * "Overdue" is defined here as: has a due date, that due date is
     * before today, AND the task is not already DONE. filter(...) keeps
     * only tasks matching that predicate; sorted() (no arguments) uses
     * Task's own compareTo(...) method to order the remaining tasks by
     * due date, soonest overdue first; toList() collects the final result.
     */
    public List<Task> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.getAll().stream()
                .filter(task -> task.getDueDate() != null
                        && task.getDueDate().isBefore(today)
                        && task.getStatus() != TaskStatus.DONE)
                .sorted()
                .toList();
    }

    /**
     * DEMONSTRATES A CUSTOM Comparator, AS AN ALTERNATIVE ORDERING TO
     * Task's NATURAL Comparable ORDER.
     * -------------------------------------------------------------------
     * Comparable defines the ONE default way to sort a class (Task sorts
     * by due date). A Comparator lets any piece of code define as many
     * alternative orderings as it needs, without ever touching the Task
     * class itself. Here we sort HIGH priority tasks first, and break ties
     * between tasks of the same priority using the due date - built by
     * chaining Comparator.comparing(...).thenComparing(...), a common,
     * readable way to build a multi-level sort.
     */
    public List<Task> getAllTasksSortedByPriority() {
        Comparator<Task> byPriorityDescendingThenDueDate = Comparator
                .comparing(Task::getPriority, Comparator.reverseOrder())
                .thenComparing(Task::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));

        return taskRepository.getAll().stream()
                .sorted(byPriorityDescendingThenDueDate)
                .toList();
    }

    public List<Task> getUrgentUnfinishedTasks() {
        return taskRepository.getUrgentUnfinishedTasks(Priority.HIGH, TaskStatus.DONE);
    }

    public Task updateTask(Long id, String title, String description, Priority priority, LocalDate dueDate) {
        Task existing = getTaskById(id);
        existing.setTitle(title);
        existing.setDescription(description);
        existing.setPriority(priority);
        existing.setDueDate(dueDate);
        return taskRepository.update(existing);
    }

    public Task changeStatus(Long id, TaskStatus newStatus) {
        Task existing = getTaskById(id);
        existing.setStatus(newStatus);
        return taskRepository.update(existing);
    }

    public void deleteTask(Long id) {
        getTaskById(id);
        taskRepository.deleteById(id);
    }
}
