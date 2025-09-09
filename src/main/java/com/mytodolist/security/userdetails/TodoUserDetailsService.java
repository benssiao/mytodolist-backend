package com.mytodolist.security.userdetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.service.UserService;

@Service
public class TodoUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public TodoUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public TodoUserDetails loadUserByUsername(String username) {
        return userService.findByUsername(username)
                .map(TodoUserDetails::new)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
    }

}
