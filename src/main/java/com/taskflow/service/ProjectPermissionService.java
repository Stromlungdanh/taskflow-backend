package com.taskflow.service;

import com.taskflow.exception.ForbiddenException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.model.Project;
import com.taskflow.repository.ProjectMemberRepository;
import com.taskflow.repository.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectPermissionService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectPermissionService(ProjectRepository projectRepository,
                                    ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public Project requireProjectExists(Long projectId) {
        Project project = projectRepository.getProjectById(projectId);
        if (project == null) {
            throw new NotFoundException("Project not found");
        }
        return project;
    }

    public void requireProjectMember(Long projectId, Long userId) {
        boolean isMember = projectMemberRepository.isMember(projectId, userId);
        if (!isMember) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    public void requireOwner(Long projectId, Long userId) {
        Project project = requireProjectExists(projectId);
        if (!project.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Only project owner can perform this action");
        }
    }

    public void requireOwnerOrAdmin(Long projectId, Long userId) {
        Project project = requireProjectExists(projectId);

        if (project.getOwnerId().equals(userId)) {
            return;
        }

        String role = projectMemberRepository.getRole(projectId, userId);
        if (!"ADMIN".equals(role)) {
            throw new ForbiddenException("You do not have permission to perform this action");
        }
    }

    public boolean isOwner(Long projectId, Long userId) {
        Project project = projectRepository.getProjectById(projectId);
        return project != null && project.getOwnerId().equals(userId);
    }

    public boolean isOwnerOrAdmin(Long projectId, Long userId) {
        Project project = projectRepository.getProjectById(projectId);
        if (project == null) return false;

        if (project.getOwnerId().equals(userId)) {
            return true;
        }

        String role = projectMemberRepository.getRole(projectId, userId);
        return "ADMIN".equals(role);
    }
}