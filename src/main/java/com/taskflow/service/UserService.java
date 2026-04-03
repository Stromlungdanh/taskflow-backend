package com.taskflow.service;

import com.taskflow.dto.UserOptionResponse;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(String username, String password) {
        User existingUser = userRepository.findByUsername(username);

        if (existingUser != null) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER"); // backend tự gán role

        userRepository.save(user);
    }
    public User login(String username, String password){

        User user = userRepository.findByUsername(username);

        if(user == null){
            throw new RuntimeException("User not found");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Password incorrect");
        }

        return user;
    }
    public List<UserOptionResponse> getAllUserOptions() {
        return userRepository.getAllUserOptions();
    }
}