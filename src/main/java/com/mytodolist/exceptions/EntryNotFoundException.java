package com.mytodolist.exceptions;

public class EntryNotFoundException extends RuntimeException {

    public EntryNotFoundException(Long entryId) {
        super("Entry not found with ID: " + entryId);
    }

}
