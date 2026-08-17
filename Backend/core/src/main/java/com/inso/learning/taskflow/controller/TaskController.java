package com.inso.learning.taskflow.controller;

import com.inso.learning.taskflow.domain.Task;
import com.inso.learning.taskflow.dto.request.TaskCreateRequest;
import com.inso.learning.taskflow.dto.request.TaskStatusUpdateRequest;
import com.inso.learning.taskflow.dto.request.TaskUpdateRequest;
import com.inso.learning.taskflow.dto.response.TaskResponse;
import com.inso.learning.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        Task created = taskService.createTask(
                request.title(), request.description(), request.priority(), request.dueDate(),
                request.projectId(), request.assigneeId(), request.tagNames());
        return ResponseEntity.created(URI.create("/api/tasks/" + created.getId()))
                .body(TaskResponse.from(created));
    }

    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id) {
        return TaskResponse.from(taskService.getTaskById(id));
    }

    @GetMapping
    public List<TaskResponse> getAll() {
        return taskService.getAllTasks().stream().map(TaskResponse::from).toList();
    }

    /**
     * PAGINATION AND SORTING THROUGH THE HTTP LAYER
     * -------------------------------------------------------------------
     * Spring MVC can build a Pageable directly from query parameters
     * (?page=0&size=20&sort=dueDate,asc) without us parsing anything by
     * hand. @PageableDefault supplies sensible defaults when the client
     * does not specify page/size/sort at all - here, 10 items per page,
     * sorted by due date ascending (soonest first).
     */
    @GetMapping(params = "projectId")
    public Page<TaskResponse> getByProject(@RequestParam Long projectId,
                                            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        return taskService.getTasksForProject(projectId, pageable).map(TaskResponse::from);
    }

    @GetMapping(params = "assigneeId")
    public List<TaskResponse> getByAssignee(@RequestParam Long assigneeId) {
        return taskService.getTasksForAssignee(assigneeId).stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/overdue")
    public List<TaskResponse> getOverdue() {
        return taskService.getOverdueTasks().stream().map(TaskResponse::from).toList();
    }

    @GetMapping("/urgent")
    public List<TaskResponse> getUrgentUnfinished() {
        return taskService.getUrgentUnfinishedTasks().stream().map(TaskResponse::from).toList();
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request) {
        Task updated = taskService.updateTask(id, request.title(), request.description(),
                request.priority(), request.dueDate());
        return TaskResponse.from(updated);
    }

    /**
     * PATCH FOR A SMALL, PARTIAL CHANGE
     * -------------------------------------------------------------------
     * This endpoint only ever changes a task's status - it does not
     * require (or accept) the title, description, priority, or due date at
     * all. This is the practical difference between PUT and PATCH: PUT
     * (above) replaces the whole editable resource, while PATCH here
     * applies one small, specific change.
     */
    @PatchMapping("/{id}/status")
    public TaskResponse changeStatus(@PathVariable Long id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        return TaskResponse.from(taskService.changeStatus(id, request.status()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
