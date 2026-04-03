package com.taskflow.controller;

import com.taskflow.dto.AuthResponse;
import com.taskflow.dto.LoginRequest;
import com.taskflow.dto.RefreshTokenRequest;
import com.taskflow.dto.RegisterRequest;
import com.taskflow.model.User;
import com.taskflow.security.JwtUtil;
import com.taskflow.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request){
        userService.register(request.getUsername(), request.getPassword());
        return "Register success";
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        userService.login(
                request.getUsername(),
                request.getPassword()
        );

        String accessToken = JwtUtil.generateAccessToken(request.getUsername());
        String refreshToken = JwtUtil.generateRefreshToken(request.getUsername());

        return new AuthResponse(accessToken, refreshToken, "Bearer");
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        String username = JwtUtil.extractUsername(refreshToken);

        if (!JwtUtil.isTokenValid(refreshToken, username)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        String newAccessToken = JwtUtil.generateAccessToken(username);

        return new AuthResponse(newAccessToken, refreshToken, "Bearer");
    }
}