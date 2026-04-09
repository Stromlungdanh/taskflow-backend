package com.taskflow.service;

import com.taskflow.dto.*;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectPermissionService projectPermissionService;

    public TaskService(TaskRepository taskRepository, ProjectMemberRepository projectMemberRepository, UserRepository userRepository, ProjectRepository projectRepository, ProjectPermissionService projectPermissionService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.projectPermissionService = projectPermissionService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) throw new RuntimeException("User not found");
        return currentUser;
    }

    private boolean isProjectManager(Long projectId, Long userId) {
        return projectPermissionService.isOwner(projectId, userId) || "ADMIN".equals(projectMemberRepository.getRole(projectId, userId));
    }

    private void checkTaskPermission(Task task, User currentUser, boolean allowAssignedUser) {
        boolean isManager = isProjectManager(task.getProjectId(), currentUser.getId());
        boolean isAssignee = task.getAssigneeId() != null && task.getAssigneeId().equals(currentUser.getId());
        if (isManager) return;
        if (allowAssignedUser && isAssignee) return;
        throw new RuntimeException("Access denied");
    }

    public Task createTask(CreateTaskRequest request) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.getProjectById(request.getProjectId());
        if (project == null) throw new RuntimeException("Project not found");
        projectPermissionService.requireProjectMember(request.getProjectId(), currentUser.getId());
        if (request.getAssigneeId() != null && !projectMemberRepository.existsByProjectIdAndUserId(request.getProjectId(), request.getAssigneeId())) {
            throw new RuntimeException("Assignee is not a member of this project");
        }
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setProjectId(request.getProjectId());
        task.setCreatedBy(currentUser.getId());
        task.setAssigneeId(request.getAssigneeId());
        task.setOrderIndex((int) (System.currentTimeMillis() / 1000));
        taskRepository.createTask(task);
        return task;
    }

    public PageResponse<TaskResponse> getTasksByProject(TaskFilterRequest request) {
        if (request.getProjectId() == null) throw new RuntimeException("Project id is required");
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        return getTasksByProject(request.getProjectId(), page, size, request.getStatus(), request.getPriority());
    }

    public PageResponse<TaskResponse> getTasksByProject(Long projectId, int page, int size, String status, String priority) {
        User currentUser = getCurrentUser();
        if (page < 0) throw new RuntimeException("Page must be greater than or equal to 0");
        if (size <= 0) throw new RuntimeException("Size must be greater than 0");
        if (size > 100) throw new RuntimeException("Size must not exceed 100");
        Project project = projectRepository.getProjectById(projectId);
        if (project == null) throw new RuntimeException("Project not found");
        projectPermissionService.requireProjectMember(projectId, currentUser.getId());
        Long assigneeFilter = null;
        int offset = page * size;
        return new PageResponse<>(
            taskRepository.findTasksByProjectIdWithFilter(projectId, status, priority, assigneeFilter, size, offset),
            page, size,
            taskRepository.countTasksByProjectIdWithFilter(projectId, status, priority, assigneeFilter),
            (int) Math.ceil((double) taskRepository.countTasksByProjectIdWithFilter(projectId, status, priority, assigneeFilter) / size)
        );
    }

    public Task updateTask(Long id, UpdateTaskRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.getTaskById(id);
        if (task == null) throw new RuntimeException("Task not found");
        projectPermissionService.requireProjectMember(task.getProjectId(), currentUser.getId());
        if (request.getAssigneeId() != null && !projectMemberRepository.existsByProjectIdAndUserId(task.getProjectId(), request.getAssigneeId())) {
            throw new RuntimeException("Assignee is not a member of this project");
        }
        checkTaskPermission(task, currentUser, false);
        int updatedRows = taskRepository.updateTask(id, request);
        if (updatedRows == 0) throw new RuntimeException("Task not found");
        return taskRepository.getTaskById(id);
    }

    public Task updateTaskStatus(Long id, UpdateTaskStatusRequest request) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.getTaskById(id);
        if (task == null) throw new RuntimeException("Task not found");
        projectPermissionService.requireProjectMember(task.getProjectId(), currentUser.getId());
        checkTaskPermission(task, currentUser, true);
        int updatedRows = taskRepository.updateTaskStatus(id, request.getStatus());
        if (updatedRows == 0) throw new RuntimeException("Task not found");
        return taskRepository.getTaskById(id);
    }

    public void deleteTask(Long id) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.getTaskById(id);
        if (task == null) throw new RuntimeException("Task not found");
        projectPermissionService.requireProjectMember(task.getProjectId(), currentUser.getId());
        checkTaskPermission(task, currentUser, false);
        if (taskRepository.deleteTask(id) == 0) throw new RuntimeException("Task not found");
    }

    public void moveTask(Long id, String status, Integer orderIndex) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.getTaskById(id);
        if (task == null) throw new RuntimeException("Task not found");
        projectPermissionService.requireProjectMember(task.getProjectId(), currentUser.getId());
        checkTaskPermission(task, currentUser, true);
        if (taskRepository.updateTaskPosition(id, status, orderIndex) == 0) throw new RuntimeException("Task not found");
    }

    public PageResponse<TaskResponse> searchTasks(TaskFilterRequest request) {
        User currentUser = getCurrentUser();
        if (request.getProjectId() == null) throw new RuntimeException("Project id is required");
        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? request.getSize() : 10;
        if (page < 0) throw new RuntimeException("Page must be greater than or equal to 0");
        if (size <= 0) throw new RuntimeException("Size must be greater than 0");
        if (size > 100) throw new RuntimeException("Size must not exceed 100");
        Project project = projectRepository.getProjectById(request.getProjectId());
        if (project == null) throw new RuntimeException("Project not found");
        projectPermissionService.requireProjectMember(request.getProjectId(), currentUser.getId());
        Long assigneeFilter = null;
        return taskRepository.searchTasks(request.getProjectId(), request.getTitle(), request.getPriority(), request.getStatus(), assigneeFilter, page, size);
    }
}
