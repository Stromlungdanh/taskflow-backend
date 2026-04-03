package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;
    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<?>> createTask(@Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task created successfully", taskService.createTask(request)));
    }

    @PostMapping("/project/tasks")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getTasksByProject(@RequestBody TaskFilterRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tasks fetched successfully", taskService.getTasksByProject(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateTask(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", taskService.updateTask(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<?>> updateTaskStatus(@PathVariable Long id, @Valid @RequestBody UpdateTaskStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Task status updated successfully", taskService.updateTaskStatus(id, request)));
    }

    @PatchMapping("/{id}/move")
    public ResponseEntity<ApiResponse<?>> moveTask(@PathVariable Long id, @RequestBody UpdateTaskPositionRequest request) {
        taskService.moveTask(id, request.getStatus(), request.getOrderIndex());
        return ResponseEntity.ok(ApiResponse.success("Task moved successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @PostMapping("/search")
    public ResponseEntity<PageResponse<TaskResponse>> searchTasks(@RequestBody TaskFilterRequest request) {
        return ResponseEntity.ok(taskService.searchTasks(request));
    }
}
