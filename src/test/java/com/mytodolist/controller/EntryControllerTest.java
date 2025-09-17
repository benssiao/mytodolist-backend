package com.mytodolist.controller;

import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.Optional;

import com.mytodolist.controllers.EntryController;
import com.mytodolist.exceptions.UserNotFoundException;
import com.mytodolist.models.Entry;
import com.mytodolist.models.User;
import com.mytodolist.services.EntryService;
import com.mytodolist.services.UserService;

@WebMvcTest(EntryController.class)
public class EntryControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private EntryService entryService;

    @MockBean
    private UserService userService;

    @Autowired
    public EntryControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testGetEntries() throws Exception {

        List<Entry> mockEntries = List.of(
                new Entry("Test entry 1", new User("testuser", "password")),
                new Entry("Test entry 2", new User("testuser", "password"))
        );

        when(entryService.getEntriesByUser(any(User.class))).thenReturn(mockEntries);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(new User("testuser", "password")));
        this.mockMvc.perform(get("/api/entries/{username}", "testuser")
                .with(user("testuser")
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    public void testGetEntries_NotFound() throws Exception {
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/entries/{username}", "nonexistent")
                .with(user("testuser").password("password")))
                .andExpect(status().isNotFound());

        verify(userService).findByUsername("nonexistent");
    }

    @Test
    public void testGetEmptyEntries() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(new User("testuser", "password")));
        when(entryService.getEntriesByUser(any(User.class))).thenReturn(List.of());

        this.mockMvc.perform(get("/api/entries/{username}", "testuser")
                .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(0)));
        verify(entryService).getEntriesByUser(any(User.class));
        verify(userService).findByUsername("testuser");
    }

    @Test
    public void testCreateEntry() throws Exception {
        Entry newEntry = new Entry();
        newEntry.setEntryBody("Test entry body");
        newEntry.setUser(new User("testuser", "password"));
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(new User("testuser", "password")));
        when(entryService.createEntry(any(Entry.class), any(User.class))).thenReturn(newEntry);

        this.mockMvc.perform(post("/api/entries")
                .with(csrf()) // Mock CSRF token
                .with(user("testuser")) // Mock authenticated user
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"entryBody\":\"Test entry body\"}")) // ✅ Close perform() here
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"));
    }

    @Test

    public void testUpdateEntry() throws Exception {

        Entry newEntry = new Entry();
        User testUser = new User("testuser", "password");
        Entry updatedEntry = new Entry("I've been updated", testUser);
        newEntry.setEntryBody("Update me");
        newEntry.setUser(testUser);
        when(entryService.getEntryById(any(Long.class))).thenReturn(Optional.of(newEntry));
        when(entryService.updateEntryById(anyLong(), any(String.class))).thenReturn(updatedEntry);
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        this.mockMvc.perform(put("/api/entries/{username}/{entryId}", "testuser", 1L)
                .with(csrf()) // Mock CSRF token
                .with(user("testuser")) // Mock authenticated user
                .contentType("application/json")
                .content("\"I've been updated\"")) // Pass the new body as a string
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.entryBody").value("I've been updated"))
                .andExpect(jsonPath("$.user.username").value("testuser"));

    }

    @Test

    public void testDeleteEntry() throws Exception {
        Entry entryToDelete = new Entry();
        entryToDelete.setId(1L);
        entryToDelete.setEntryBody("Entry to delete");
        entryToDelete.setUser(new User("testuser", "password"));
        when(entryService.getEntryById(anyLong())).thenReturn(Optional.of(entryToDelete));
        this.mockMvc.perform(delete("/api/entries/{entryId}", 1L)
                .with(csrf())
                .with(user("testuser")))
                .andExpect(status().isNoContent());

        verify(entryService).deleteEntryById(1L);

    }

}
