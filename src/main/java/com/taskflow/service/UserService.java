package com.taskflow.service;

import com.taskflow.dto.UserOptionResponse;
import com.taskflow.model.User;
import com.taskflow.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void register(User user){
        userRepository.save(user);
    }
    public User login(String username, String password){

        User user = userRepository.findByUsername(username);

        if(user == null){
            throw new RuntimeException("User not found");
        }

        if(!user.getPassword().equals(password)){
            throw new RuntimeException("Password incorrect");
        }

        return user;
    }
    public List<UserOptionResponse> getAllUserOptions() {
        return userRepository.getAllUserOptions();
    }
}