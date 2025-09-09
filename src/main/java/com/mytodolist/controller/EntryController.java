package com.mytodolist.controller;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import com.mytodolist.dto.EntryDTO;
import com.mytodolist.exceptions.EntryNotFoundException;
import com.mytodolist.model.Entry;
import com.mytodolist.model.User;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.service.EntryService;

@Controller
@RequestMapping(path = "/api/v1/entries", produces = "application/json")
@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from frontend
public class EntryController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EntryController.class);

    private final EntryService entryService;

    public EntryController(EntryService entryService) {
        this.entryService = entryService;
    }

    @GetMapping
    @ResponseBody
    public List<Entry> getEntries(Authentication auth) {
        // turn this into a pagination version later.

        User user = ((TodoUserDetails) auth.getPrincipal()).getUser();
        return entryService.getEntriesByUser(user);
    }

    @PostMapping
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public Entry createEntry(@RequestBody EntryDTO entryDTO, Authentication auth) {
        Entry entry = new Entry();
        User user = ((TodoUserDetails) auth.getPrincipal()).getUser();
        entry.setEntryBody(entryDTO.getEntryBody());
        entry.setUser(user);
        return entryService.createEntry(entry, user);
    }

    @PutMapping("/{entryId}")
    @ResponseBody
    public Entry updateEntry(@PathVariable Long entryId, @RequestBody String newBody, Authentication auth) {
        String loggedInUsername = ((TodoUserDetails) auth.getPrincipal()).getUser().getUsername();
        Entry entry = entryService.getEntryById(entryId).orElseThrow(() -> new EntryNotFoundException("Entry with id " + entryId + " not found"));

        if (!entry.getUser().getUsername().equals(loggedInUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this entry.");
        }

        return entryService.updateEntryById(entryId, newBody);
    }

    @DeleteMapping("/{entryId}")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEntry(@PathVariable Long entryId, Authentication auth) {
        String loggedInUsername = ((TodoUserDetails) auth.getPrincipal()).getUser().getUsername();
        Entry entry = entryService.getEntryById(entryId).orElseThrow(() -> new EntryNotFoundException("Entry with id " + entryId + " not found"));

        if (!entry.getUser().getUsername().equals(loggedInUsername)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to delete this entry.");
        }
        entryService.deleteEntryById(entryId);
    }

}
