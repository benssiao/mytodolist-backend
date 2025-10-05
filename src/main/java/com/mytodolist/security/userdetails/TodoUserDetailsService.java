package com.mytodolist.security.userdetails;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.models.User;
import com.mytodolist.security.services.RoleService;
import com.mytodolist.services.UserService;

@Service
public class TodoUserDetailsService implements UserDetailsService {

    private final UserService userService;
    private final RoleService roleService;

    public TodoUserDetailsService(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @Override
    public TodoUserDetails loadUserByUsername(String username) {

        User user = userService.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + username));
        return new TodoUserDetails(user, roleService);
    }

}
