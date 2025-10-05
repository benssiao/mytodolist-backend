package com.mytodolist.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mytodolist.controllers.UserController;
import com.mytodolist.models.User;
import com.mytodolist.services.EntryService;
import com.mytodolist.services.UserService;

import org.mockito.ArgumentCaptor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.util.Optional;

import com.mytodolist.security.filters.JwtAuthFilter;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.services.RoleService;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.security.userdetails.TodoUserDetailsService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import java.util.Set;
import com.mytodolist.security.config.SecurityConfig;
import org.springframework.context.annotation.Import;
import com.mytodolist.exceptions.GlobalExceptionHandler;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)

public class UserControllerTest {

    private final MockMvc mockMvc;

    @MockBean
    private EntryService entryService;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TodoUserDetailsService todoUserDetailsService;

    @MockBean
    private RoleService roleService;

    @Autowired
    public UserControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private Authentication createMockAuthentication(User user) {
        TodoUserDetails userDetails = new TodoUserDetails(user, roleService);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private User createTestUser() {
        User user = new User("testuser", "password");
        user.setId(1L);
        return user;
    }

    @BeforeEach
    void setup() {
        when(roleService.getUserRoles(1L)).
                thenReturn(Set.of("ROLE_USER"));
    }

    @Test
    public void testGetUserProfile() throws Exception {
        User testUser = createTestUser();
        Authentication mockAuth = createMockAuthentication(testUser);

        this.mockMvc.perform(get("/api/v1/users/me")
                .with(authentication(mockAuth))
                .with(csrf())
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));

    }

    @Test
    public void testGetUserProfile_Unauthenticated() throws Exception {

        this.mockMvc.perform(get("/api/v1/users/me")
                .with(csrf())
                .contentType("application/json"))
                .andExpect(status().isUnauthorized());

    }
}
