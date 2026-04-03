package com.taskflow.dto;

public class ProjectPermissionResponse {
    private Long userId;
    private String projectRole;
    private boolean canManageProject;
    private boolean canManageTasks;
    private boolean canManageMembers;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getProjectRole() { return projectRole; }
    public void setProjectRole(String projectRole) { this.projectRole = projectRole; }
    public boolean isCanManageProject() { return canManageProject; }
    public void setCanManageProject(boolean canManageProject) { this.canManageProject = canManageProject; }
    public boolean isCanManageTasks() { return canManageTasks; }
    public void setCanManageTasks(boolean canManageTasks) { this.canManageTasks = canManageTasks; }
    public boolean isCanManageMembers() { return canManageMembers; }
    public void setCanManageMembers(boolean canManageMembers) { this.canManageMembers = canManageMembers; }
}
