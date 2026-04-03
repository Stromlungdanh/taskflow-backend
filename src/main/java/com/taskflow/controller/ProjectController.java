package com.taskflow.controller;

import com.taskflow.dto.*;
import com.taskflow.model.Project;
import com.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<?>> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Project created successfully", projectService.createProject(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getProjects() {
        return ResponseEntity.ok(ApiResponse.success("Projects fetched successfully", projectService.getAllProjects()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Project>> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Project fetched successfully", projectService.getProjectById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> updateProject(@PathVariable Long id, @RequestBody UpdateProjectRequest request) {
        projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @GetMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<List<UserOptionResponse>>> getProjectMembers(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Project members fetched successfully", projectService.getProjectMembers(projectId)));
    }

    @GetMapping("/{projectId}/permission")
    public ResponseEntity<ApiResponse<ProjectPermissionResponse>> getMyProjectPermission(@PathVariable Long projectId) {
        return ResponseEntity.ok(ApiResponse.success("Project permission fetched successfully", projectService.getMyProjectPermission(projectId)));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ApiResponse<?>> addProjectMember(@PathVariable Long projectId, @RequestBody AddProjectMemberRequest request) {
        projectService.addProjectMember(projectId, request);
        return ResponseEntity.ok(ApiResponse.success("Member added successfully", null));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ApiResponse<?>> removeProjectMember(@PathVariable Long projectId, @PathVariable Long userId) {
        projectService.removeProjectMember(projectId, userId);
        return ResponseEntity.ok(ApiResponse.success("Member removed successfully", null));
    }
}
