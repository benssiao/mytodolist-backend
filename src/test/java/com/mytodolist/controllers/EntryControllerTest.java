package com.mytodolist.controllers;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytodolist.controllers.EntryController;
import com.mytodolist.customauthtoken.WithCustomUser;
import com.mytodolist.dtos.EntryDTO;
import com.mytodolist.models.Entry;
import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtAuthenticationEntryPoint;
import com.mytodolist.security.providers.UsernamePasswordAuthenticationProvider;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.services.EntryService;
import com.mytodolist.services.UserService;
import com.mytodolist.security.config.SecurityConfig;
import com.mytodolist.security.userdetails.TodoUserDetails;
import java.time.LocalDateTime;

@WebMvcTest(EntryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class EntryControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private EntryService entryService;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtUtilityService jwtUtilityService;
    @MockBean
    private com.mytodolist.security.services.RoleService roleService;
    @MockBean
    private UsernamePasswordAuthenticationProvider authenticationProvider;
    @MockBean
    private com.mytodolist.security.userdetails.TodoUserDetailsService todoUserDetailsService;
    @MockBean
    private com.mytodolist.security.filters.JwtAuthFilter jwtAuthFilter;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EntryControllerTest.class);

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testGetEntries() throws Exception {

        List<Entry> mockEntries = List.of(
                new Entry("Test entry 1"),
                new Entry("Test entry 2")
        );

        when(entryService.getEntriesByUser(any(User.class))).thenReturn(mockEntries);
        logger.info("Mock entries: {}", mockEntries);

        this.mockMvc.perform(get("/api/v1/entries")
                .with(csrf())
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testGetEntries_NoEntries() throws Exception {

        when(entryService.getEntriesByUser(any(User.class))).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/entries")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

    /* 
    @Test
        @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testGetEntries_ServiceException() throws Exception {
        User testUser = createTestUser();
        Authentication mockAuth = createMockAuthentication(testUser);

        when(entryService.getEntriesByUser(testUser)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/v1/entries")
                .with(authentication(mockAuth)))
                .andExpect(status().isInternalServerError()); // Or whatever your @ExceptionHandler returns
    }

    @Test
    public void testGetEntries_Unauthenticated() throws Exception {
        // No authentication provided
        mockMvc.perform(get("/api/v1/entries"))
                .andExpect(status().isUnauthorized());
    }
     */
    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testCreateEntry() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TodoUserDetails principal = (TodoUserDetails) auth.getPrincipal();
        User testUser = principal.getUser();
        Entry newEntry = new Entry("Test entry body", testUser);
        String newEntryJson = objectMapper.writeValueAsString(newEntry);
        when(entryService.createEntry(any(Entry.class), any(User.class))).thenReturn(newEntry);
        this.mockMvc.perform(post("/api/v1/entries")
                .with(csrf())
                .contentType("application/json")
                .content(newEntryJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.entryBody").value("Test entry body"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testCreateEntry_ValidationError() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TodoUserDetails principal = (TodoUserDetails) auth.getPrincipal();
        User testUser = principal.getUser();
        String body5001 = "a".repeat(5001);
        Entry invalidEntry = new Entry(body5001, testUser);
        
        String invalidJson = objectMapper.writeValueAsString(invalidEntry);
        
        
        when(entryService.createEntry(any(Entry.class), any(User.class))).thenReturn(invalidEntry);
        mockMvc.perform(post("/api/v1/entries")
                .with(csrf())
                .contentType("application/json")
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testUpdateEntry_Unauthorized() throws Exception {

        User otherUser = new User("otheruser", "password");
        otherUser.setId(2L);
        Entry entryOwnedByOther = new Entry("Other's entry", otherUser);
        entryOwnedByOther.setId(1L);

        when(entryService.getEntryById(1L)).thenReturn(Optional.of(entryOwnedByOther));

        EntryDTO updateDTO = new EntryDTO();
        updateDTO.setEntryBody("I've been updated");
        String updateJson = objectMapper.writeValueAsString(updateDTO);

        this.mockMvc.perform(put("/api/v1/entries/{entryId}", 1L)
                .with(csrf())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isForbidden());

        verify(entryService).getEntryById(1L);
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testUpdateEntry_NotFound() throws Exception {
        EntryDTO updateDTO = new EntryDTO();
        updateDTO.setEntryBody("I've been updated");
        String updateJson = objectMapper.writeValueAsString(updateDTO);
        when(entryService.getEntryById(any(Long.class))).thenReturn(Optional.empty());
        this.mockMvc.perform(put("/api/v1/entries/{entryId}", 1L)
                .with(csrf())
                .contentType("application/json")
                .content(updateJson
                ))
                .andExpect(status().isNotFound());
        verify(entryService).getEntryById(1L);
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testUpdateEntry() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TodoUserDetails principal = (TodoUserDetails) auth.getPrincipal();
        User testUser = principal.getUser();
        logger.info("testUser = {}", testUser
        );
        Entry newEntry = new Entry("Update me", testUser);
        newEntry.setId(1L);
        Entry updatedEntry = new Entry("I've been updated", testUser);
        when(entryService.getEntryById(eq(1L))).thenReturn(Optional.of(newEntry));
        when(entryService.updateEntryById(eq(1L), any(String.class))).thenReturn(updatedEntry);

        EntryDTO updateDTO = new EntryDTO(); // for serialization
        updateDTO.setEntryBody("I've been updated");
        String updateJson = objectMapper.writeValueAsString(updateDTO);
        this.mockMvc.perform(put("/api/v1/entries/{entryId}", 1L)
                .with(csrf())
                .contentType("application/json")
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.entryBody").value("I've been updated"))
                .andExpect(jsonPath("$.username").value("testuser"));
        verify(entryService).updateEntryById(eq(1L), eq("I've been updated"));
        verify(entryService).getEntryById(eq(1L));
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testDeleteEntry() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TodoUserDetails principal = (TodoUserDetails) auth.getPrincipal();
        User testUser = principal.getUser();
        Entry entryToDelete = new Entry("Entry to delete", testUser);
        when(entryService.getEntryById(1L)).thenReturn(Optional.of(entryToDelete));
        this.mockMvc.perform(delete("/api/v1/entries/{entryId}", 1L)
                .with(csrf())
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isNoContent());

        verify(entryService).deleteEntryById(1L);

    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testDeleteEntry_Unauthorized() throws Exception {
        User otherUser = new User("otheruser", "password");
        otherUser.setId(2L);
        Entry entryOwnedByOther = new Entry("Entry by other user", otherUser);
        entryOwnedByOther.setId(1L);

        when(entryService.getEntryById(1L)).thenReturn(Optional.of(entryOwnedByOther));

        mockMvc.perform(delete("/api/v1/entries/{entryId}", 1L)
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(entryService).getEntryById(1L);
    }

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testDeleteEntry_NotFound() throws Exception {
        when(entryService.getEntryById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/v1/entries/{entryId}", 1L)
                .with(csrf()))
                .andExpect(status().isNotFound());
    }
}
