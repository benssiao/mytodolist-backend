package com.mytodolist.exceptions;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }

    public UserNotFoundException(String username) {
        super("User not found with username: " + username);
    }

}
