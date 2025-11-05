package com.mytodolist.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mytodolist.exceptions.UnauthenticatedAccessException;
import com.mytodolist.models.User;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.services.UserService;
import java.util.Set;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    record MeDTO(Long id, String username, Set<String> roles) {

    }

    public UserController(@Valid UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public MeDTO getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthenticatedAccessException("User is not authenticated");
        }
        TodoUserDetails userDetails = (TodoUserDetails) auth.getPrincipal();
        return new MeDTO(userDetails.getUser().getId(), userDetails.getUsername(), userDetails.getRoles());

    }
