package com.mytodolist.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.mytodolist.dto.UserDTO;
import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.model.User;
import com.mytodolist.service.UserService;

@Controller
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from frontend
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        logger.info("Initializing UserController with UserService and UserRepository");
        this.userService = userService;

    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        //logger.info("Creating user with username: " + user.getUsername() + " and password: " + user.getPassword());
        return userService.createUser(user);
    }

    @GetMapping("/{username}")
    @ResponseBody
    public User getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
    }

}
