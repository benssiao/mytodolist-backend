package com.mytodolist.controller;

import java.util.List;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.mytodolist.model.Entry;
import com.mytodolist.model.User;
import com.mytodolist.service.EntryService;

import org.springframework.beans.factory.annotation.Autowired;

import com.mytodolist.controller.EntryController;
import com.mytodolist.service.UserService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

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

        this.mockMvc.perform(get("/api/entries/{username}", "testuser")
                .with(user("testuser")
                ))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));

    }

    @Test
    public void testCreateEntry() throws Exception {
        Entry newEntry = new Entry();
        newEntry.setEntryBody("Test entry body");
        newEntry.setUser(new User("testuser", "password"));
        when(entryService.createEntry(any(Entry.class), any(User.class))).thenReturn(newEntry);
        this.mockMvc.perform(post("/api/entries")
                .with(csrf()) // Mock CSRF token
                .with(user("testuser")) // Mock authenticated user
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"entryBody\":\"Test entry body\"}")) // ✅ Close perform() here
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    @Disabled
    public void testUpdateEntry() throws Exception {
        // Implement test logic here
    }

    @Test
    @Disabled
    public void testDeleteEntry() throws Exception {
        // Implement test logic here
    }

}
