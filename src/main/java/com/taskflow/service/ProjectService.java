package com.taskflow.service;

import com.taskflow.dto.AddProjectMemberRequest;
import com.taskflow.dto.CreateProjectRequest;
import com.taskflow.dto.ProjectPermissionResponse;
import com.taskflow.dto.UpdateProjectRequest;
import com.taskflow.dto.UserOptionResponse;
import com.taskflow.exception.ForbiddenException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.model.Project;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final TaskRepository taskRepository;
    private final ProjectPermissionService projectPermissionService;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository, ProjectMemberRepository projectMemberRepository, TaskRepository taskRepository, ProjectPermissionService projectPermissionService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.taskRepository = taskRepository;
        this.projectPermissionService = projectPermissionService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) throw new NotFoundException("User not found");
        return currentUser;
    }

    public Project createProject(CreateProjectRequest request) {
        User currentUser = getCurrentUser();
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setOwnerId(currentUser.getId());
        Long projectId = projectRepository.createProject(project);
        projectMemberRepository.addMember(projectId, currentUser.getId(), "OWNER");
        return projectRepository.getProjectById(projectId);
    }

    public List<Project> getAllProjects() {
        User currentUser = getCurrentUser();
        return projectRepository.getProjectsByUserId(currentUser.getId());
    }

    public Project getProjectById(Long id) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.getProjectById(id);
        if (project == null) throw new NotFoundException("Project not found");
        projectPermissionService.requireProjectMember(id, currentUser.getId());
        return project;
    }

    public void updateProject(Long id, UpdateProjectRequest request) {
        User currentUser = getCurrentUser();
        projectPermissionService.requireOwnerOrAdmin(id, currentUser.getId());
        int updatedRows = projectRepository.updateProject(id, request);
        if (updatedRows == 0) throw new NotFoundException("Project not found");
    }

    public void deleteProject(Long id) {
        User currentUser = getCurrentUser();
        projectPermissionService.requireOwner(id, currentUser.getId());
        int deletedRows = projectRepository.deleteProject(id);
        if (deletedRows == 0) throw new NotFoundException("Project not found");
    }

    public List<UserOptionResponse> getProjectMembers(Long projectId) {
        User currentUser = getCurrentUser();
        projectPermissionService.requireProjectExists(projectId);
        projectPermissionService.requireProjectMember(projectId, currentUser.getId());
        return projectMemberRepository.getMembersByProjectId(projectId);
    }

    public ProjectPermissionResponse getMyProjectPermission(Long projectId) {
        User currentUser = getCurrentUser();
        Project project = projectRepository.getProjectById(projectId);
        if (project == null) throw new NotFoundException("Project not found");
        projectPermissionService.requireProjectMember(projectId, currentUser.getId());
        String role = projectMemberRepository.getRole(projectId, currentUser.getId());
        if (role == null && project.getOwnerId().equals(currentUser.getId())) role = "OWNER";
        ProjectPermissionResponse response = new ProjectPermissionResponse();
        response.setUserId(currentUser.getId());
        response.setProjectRole(role);
        boolean isOwner = "OWNER".equals(role);
        boolean isAdmin = "ADMIN".equals(role);
        response.setCanManageProject(isOwner || isAdmin);
        response.setCanManageTasks(isOwner || isAdmin);
        response.setCanManageMembers(isOwner || isAdmin);
        return response;
    }

    public void addProjectMember(Long projectId, AddProjectMemberRequest request) {
        User currentUser = getCurrentUser();
        projectPermissionService.requireOwnerOrAdmin(projectId, currentUser.getId());
        if (request.getUserId() == null) throw new RuntimeException("User id is required");
        User user = userRepository.findById(request.getUserId());
        if (user == null) throw new RuntimeException("User not found");
        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, request.getUserId())) throw new RuntimeException("User is already a member of this project");
        String role = request.getRole() == null || request.getRole().isBlank() ? "MEMBER" : request.getRole().toUpperCase();
        if (!role.equals("ADMIN") && !role.equals("MEMBER")) throw new RuntimeException("Role must be ADMIN or MEMBER");
        if ("ADMIN".equals(role) && !projectPermissionService.isOwner(projectId, currentUser.getId())) throw new ForbiddenException("Only owner can add ADMIN");
        projectMemberRepository.addMember(projectId, request.getUserId(), role);
    }

    public void removeProjectMember(Long projectId, Long userId) {
        User currentUser = getCurrentUser();
        projectPermissionService.requireOwnerOrAdmin(projectId, currentUser.getId());
        User user = userRepository.findById(userId);
        if (user == null) throw new NotFoundException("User not found");
        boolean exists = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!exists) throw new NotFoundException("User is not a member of this project");
        String targetRole = projectMemberRepository.getRole(projectId, userId);
        if ("OWNER".equals(targetRole)) throw new ForbiddenException("Owner cannot be removed");
        if ("ADMIN".equals(targetRole) && !projectPermissionService.isOwner(projectId, currentUser.getId())) throw new ForbiddenException("Only owner can remove ADMIN");
        if (taskRepository.existsByProjectIdAndAssigneeId(projectId, userId)) throw new RuntimeException("User is assigned to tasks in this project");
        int deletedRows = projectMemberRepository.removeMember(projectId, userId);
        if (deletedRows == 0) throw new RuntimeException("Remove member failed");
    }
}
