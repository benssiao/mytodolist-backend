package com.mytodolist.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytodolist.customauthtoken.WithCustomUser;
import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtAuthenticationEntryPoint;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.filters.JwtAuthFilter;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.security.userdetails.TodoUserDetailsService;
import com.mytodolist.services.EntryService;
import com.mytodolist.services.UserService;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.springframework.security.authentication.AuthenticationManager;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder; // need this because UserController has PasswordEncoder as dependency

    @MockBean
    JwtAuthFilter jwtAuthFilter; // need this because of security filter chain. In general mockbean all securityconfig dependencies.

    @Test
    @WithCustomUser(username = "testuser", roles = {"USER"}, password = "password")
    public void testGetUserProfile() throws Exception {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TodoUserDetails principal = (TodoUserDetails) auth.getPrincipal();
        User testUser = principal.getUser();

        this.mockMvc.perform(get("/api/v1/users/me")
                .with(csrf())
                .contentType("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("testuser"));

    }

    @Test
    public void testGetUserProfile_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
