package com.mytodolist.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mytodolist.security.dtos.LogoutRequestDTO;
import com.mytodolist.security.dtos.RegisterRequestDTO;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class AuthRefreshIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void refreshTokenSuccess() throws Exception {
        RegisterRequestDTO register = new RegisterRequestDTO("user" + System.currentTimeMillis(), "Password1");
        String json = objectMapper.writeValueAsString(register);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf()).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isOk()).andReturn();

        String refreshToken = JsonPath.read(login.getResponse().getContentAsString(), "$.refreshToken");

        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void logoutInvalidatesRefreshToken() throws Exception {
        String username = "user" + System.currentTimeMillis();
        RegisterRequestDTO register = new RegisterRequestDTO(username, "Password1");
        String regJson = objectMapper.writeValueAsString(register);

        mockMvc.perform(post("/api/v1/auth/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(regJson))
                .andExpect(status().isCreated());

        MvcResult tokenResult = mockMvc.perform(post("/api/v1/auth/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(regJson))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = JsonPath.read(tokenResult.getResponse().getContentAsString(), "$.refreshToken");

        LogoutRequestDTO logoutRequest = new LogoutRequestDTO(username);
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logoutRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User logged out successfully"));

        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isForbidden()) // because UnauthorizedAccessException → 403
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}
