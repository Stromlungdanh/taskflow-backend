package com.taskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class UpdateTaskStatusRequest {

    @NotBlank(message = "Status must not be blank")
    @Pattern(
            regexp = "TODO|IN_PROGRESS|DONE",
            message = "Status must be one of: TODO, IN_PROGRESS, DONE"
    )
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}