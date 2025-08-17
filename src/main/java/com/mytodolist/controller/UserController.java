package com.mytodolist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mytodolist.dto.UserDTO;
import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.model.User;
import com.mytodolist.repository.UserRepository;
import com.mytodolist.service.UserService;

import jakarta.annotation.PostConstruct;

@Controller
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from frontend
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;

    @PostConstruct
    public void init() {
        logger.info("=== UserController initialized and registered ===");
        logger.info("Mapped to /api/users");
    }

    public UserController(UserService userService, UserRepository userRepository) {
        logger.info("Initializing UserController with UserService and UserRepository");
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{username}")
    @ResponseBody
    public User getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

    @GetMapping
    @ResponseBody
    public String test() {
        logger.info("Test endpoint hit");

        return "UserController is working!";
        // This method is just for testing purposes, it can be removed later.
        // It can be used to check if the controller is working.
        // You can return a simple message or status if needed.
    }

    @PostMapping
    @ResponseBody
    public User createUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        logger.info("Creating user with username: " + user.getUsername() + " and password: " + user.getPassword());
        return userService.createUser(user);
    }

}
