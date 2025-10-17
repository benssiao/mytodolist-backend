package com.mytodolist.controllers;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.mytodolist.dtos.EntryDTO;
import com.mytodolist.dtos.EntryResponseDTO;
import com.mytodolist.exceptions.EntryNotFoundException;
import com.mytodolist.exceptions.UnauthorizedAccessException;
import com.mytodolist.models.Entry;
import com.mytodolist.models.User;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.services.EntryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/api/v1/entries", produces = "application/json")
@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from frontend
public class EntryController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EntryController.class);

    private final EntryService entryService;

    public EntryController(EntryService entryService) {

        this.entryService = entryService;
    }

    @GetMapping
    public List<Entry> getEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // turn this into a pagination version later.

        User user = ((TodoUserDetails) auth.getPrincipal()).getUser();
        return entryService.getEntriesByUser(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EntryResponseDTO createEntry(@Valid @RequestBody EntryDTO entryDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Entry entry = new Entry();
        User user = ((TodoUserDetails) auth.getPrincipal()).getUser();
        entry.setEntryBody(entryDTO.getEntryBody());
        entry.setUser(user);
        EntryResponseDTO response = new EntryResponseDTO(entryService.createEntry(entry, user));
        return response;
    }

    @PutMapping("/{entryId}")
    public EntryResponseDTO updateEntry(@PathVariable Long entryId, @Valid @RequestBody EntryDTO entryDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String newBody = entryDTO.getEntryBody();
        String loggedInUsername = ((TodoUserDetails) auth.getPrincipal()).getUser().getUsername();
        Entry entry = entryService.getEntryById(entryId).orElseThrow(() -> new EntryNotFoundException(entryId));
        if (!entry.getUser().getUsername().equals(loggedInUsername)) {
            throw new UnauthorizedAccessException("You do not have permission to update this entry.");
        }
        EntryResponseDTO response = new EntryResponseDTO(entryService.updateEntryById(entryId, newBody));
        return response;
    }

    @DeleteMapping("/{entryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long entryId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String loggedInUsername = ((TodoUserDetails) auth.getPrincipal()).getUser().getUsername();
        Entry entry = entryService.getEntryById(entryId).orElseThrow(() -> new EntryNotFoundException(entryId));

        if (!entry.getUser().getUsername().equals(loggedInUsername)) {
            throw new UnauthorizedAccessException("You do not have permission to delete this entry.");
        }
        entryService.deleteEntryById(entryId);
    }

}
