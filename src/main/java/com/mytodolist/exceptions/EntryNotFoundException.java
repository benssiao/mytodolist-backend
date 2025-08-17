package com.mytodolist.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EntryNotFoundException extends ResponseStatusException {

    public EntryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "Entry not found");
    }

    public EntryNotFoundException(Long entryId) {
        super(HttpStatus.NOT_FOUND, "Entry not found with ID: " + entryId);
    }

    public EntryNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

}
