package com.mytodolist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import com.mytodolist.service.UserService;

import com.mytodolist.model.User;

import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import java.util.Optional;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    private final MockMvc mockMvc;
    @MockBean
    private UserService userService;

    @Autowired
    public UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    public void testCreateUser() throws Exception {

        User toBeCreatedUser = new User("username", "password");
        when(userService.createUser(any(User.class))).thenReturn(toBeCreatedUser);

        this.mockMvc.perform(post("/api/users")
                .with(csrf())
                .with(user("username").password("password"))
                .contentType("application/json")
                .content("{\"username\":\"username\", \"password\":\"password\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("username"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).createUser(userCaptor.capture());
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo("username");
        assertThat(capturedUser.getPassword()).isEqualTo("password");

    }

    @Test
    public void testGetUserByUsername() throws Exception {
        User mockUser = new User("testuser", "password");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(mockUser));
        this.mockMvc.perform(get("/api/users/{username}", "testuser")
                .with(user("testuser").password("password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).findByUsername("testuser");

    }

    @Test
    public void testGetUserByUsername_NotFound() throws Exception {
        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/users/{username}", "nonexistent")
                .with(user("testuser").password("password")))
                .andExpect(status().isNotFound()); // or whatever your controller throws
    }

}
