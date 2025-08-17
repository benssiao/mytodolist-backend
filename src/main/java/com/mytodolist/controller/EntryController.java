package com.mytodolist.controller;

import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import com.mytodolist.dto.EntryDTO;
import com.mytodolist.exceptions.EntryNotFoundException;
import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.model.Entry;
import com.mytodolist.model.User;
import com.mytodolist.service.EntryService;
import com.mytodolist.service.UserService;

@Controller
@RequestMapping(path = "/api/entries", produces = "application/json")
@CrossOrigin(origins = "http://localhost:5173") // Allow cross-origin requests from frontend
public class EntryController {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EntryController.class);

    private final EntryService entryService;
    private final UserService userService;

    public EntryController(EntryService entryService, UserService userService) {
        this.entryService = entryService;
        this.userService = userService;

    }

    @GetMapping("/{username}")
    @ResponseBody
    public List<Entry> getEntries(@PathVariable String username) {
        // turn this into a pagination version later.
        return entryService.getEntriesByUser(userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username)));
    }

    @PostMapping
    @ResponseBody
    public Entry postEntity(@RequestBody EntryDTO entryDTO) {
        logger.info("Creating new entry for user: {}", entryDTO.getUsername());
        logger.info("Entry body: {}", entryDTO.getEntryBody());
        Entry entry = new Entry();
        User user = userService.findByUsername(entryDTO.getUsername()).orElseThrow(() -> new UserNotFoundException(entryDTO.getUsername()));
        entry.setEntryBody(entryDTO.getEntryBody());
        entry.setUser(user);
        return entryService.createEntry(entry, user);
    }

    @PutMapping("/{username}/{entryId}")
    @ResponseBody
    public Entry updateEntry(@PathVariable String username, @PathVariable Long entryId, @RequestBody String newBody) {
        Entry entry = entryService.getEntryById(entryId).orElseThrow(() -> new EntryNotFoundException(username));
        if (!entry.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to update this entry.");
        }
        return entryService.updateEntryById(entryId, newBody);
    }

    @DeleteMapping("/{entryId}")
    public void deleteEntry(@PathVariable Long entryId) {
        entryService.deleteEntryById(entryId);
    }

}
