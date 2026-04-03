package com.taskflow.controller;

import com.taskflow.dto.ApiResponse;
import com.taskflow.dto.UserOptionResponse;
import com.taskflow.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserOptionResponse>>> getAllUsers() {
        List<UserOptionResponse> users = userService.getAllUserOptions();
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
    }
}