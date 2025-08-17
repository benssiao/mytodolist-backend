package com.mytodolist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "User not found");
    }

    public UserNotFoundException(Long userId) {
        super(HttpStatus.NOT_FOUND, "User not found with ID: " + userId);
    }

    public UserNotFoundException(String username) {
        super(HttpStatus.NOT_FOUND, "User not found with username: " + username);
    }

}
