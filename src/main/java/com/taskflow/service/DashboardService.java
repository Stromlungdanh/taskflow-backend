package com.taskflow.service;

import com.taskflow.dto.DashboardSummaryResponse;
import com.taskflow.exception.NotFoundException;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;

    public DashboardService(UserRepository userRepository, ProjectRepository projectRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username);
        if (currentUser == null) throw new NotFoundException("User not found");
        return currentUser;
    }

    public DashboardSummaryResponse getSummary() {
        User currentUser = getCurrentUser();
        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalProjects(projectRepository.countProjectsByUserId(currentUser.getId()));
        response.setTotalTasks(taskRepository.countTasksInProjectsByUserId(currentUser.getId()));
        response.setTodoTasks(taskRepository.countTasksInProjectsByUserIdAndStatus(currentUser.getId(), "TODO"));
        response.setInProgressTasks(taskRepository.countTasksInProjectsByUserIdAndStatus(currentUser.getId(), "IN_PROGRESS"));
        response.setDoneTasks(taskRepository.countTasksInProjectsByUserIdAndStatus(currentUser.getId(), "DONE"));
        return response;
    }
}
