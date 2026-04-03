package com.taskflow.dto;

import jakarta.validation.constraints.*;

public class CreateTaskRequest {

    @NotBlank(message = "Task title must not be blank")
    @Size(max = 100, message = "Task title must not exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Task description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Status must not be blank")
    @Pattern(
            regexp = "TODO|IN_PROGRESS|DONE",
            message = "Status must be one of: TODO, IN_PROGRESS, DONE"
    )
    @NotBlank private String status;


    @NotBlank(message = "Priority must not be blank")
    @Pattern(
            regexp = "LOW|MEDIUM|HIGH",
            message = "Priority must be one of: LOW, MEDIUM, HIGH"
    )
    private String priority;

    @NotNull(message = "Project ID must not be null")
    @Positive(message = "Project ID must be greater than 0")
    private Long projectId;

    private Long assigneeId;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}
