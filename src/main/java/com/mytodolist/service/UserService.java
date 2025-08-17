package com.mytodolist.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mytodolist.model.User;
import com.mytodolist.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //CREATE
    public User createUser(User user) {
        userRepository.save(user);
        return userRepository.save(user);
    }

    //READ
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    //UPDATE
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    //DELETE
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    //validation
    public boolean validateUser(String username, String password) {
        // Logic to validate user credentials
        return true; // Placeholder return statement
    }

}
