package com.mytodolist.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.mytodolist.models.User;
import com.mytodolist.repositories.UserRepository;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.dtos.RegisterRequestDTO;
import com.mytodolist.security.models.RefreshToken;
import com.mytodolist.security.repositories.RefreshTokenRepository;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.services.RefreshTokenService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
public class VerifyTokenIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    JwtUtilityService jwtUtilityService;
    @Autowired
    JwtConfig jwtConfig;
    @Autowired
    RefreshTokenService refreshTokenService;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    UserRepository userRepository;

    @Test
    void verifyAccessToken_ShouldReturn200() throws Exception {

        RegisterRequestDTO registerData = new RegisterRequestDTO("user" + System.currentTimeMillis(), "Password1");
        String registerDataDTO = objectMapper.writeValueAsString(registerData);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerDataDTO))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerDataDTO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();
        String accessToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.accessToken");

        mockMvc.perform(post("/api/v1/auth/verifyaccess")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"accessToken\":\"" + accessToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Access token is valid"));
    }

    @Test
    void verifyRefreshToken_ShouldReturn200() throws Exception {

        RegisterRequestDTO registerData = new RegisterRequestDTO("user" + System.currentTimeMillis(), "Password1");
        String registerDataDTO = objectMapper.writeValueAsString(registerData);

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerDataDTO))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerDataDTO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();
        String refreshToken = JsonPath.read(loginResult.getResponse().getContentAsString(), "$.refreshToken");

        mockMvc.perform(post("/api/v1/auth/verifyrefresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Refresh token is valid"));

    }

    @Test
    void verifyAccessTokenTimedOut_ShouldReturn401() throws Exception {
        Instant timeNow = jwtUtilityService.getClock().instant();
        Clock pastClock = Clock.fixed(timeNow.minusMillis(2 * jwtConfig.getExpiration()), ZoneOffset.UTC); // because my clock is standardized to UTC
        JwtUtilityService expiredJwtUtilityService = new JwtUtilityService(jwtConfig, pastClock);
        User user = new User("testuser", "Password1");
        String expiredToken = expiredJwtUtilityService.generateToken(user);

        assertFalse(jwtUtilityService.validateToken(expiredToken));

        mockMvc.perform(post("/api/v1/auth/verifyaccess")
                .header("Authorization", "Bearer " + expiredToken)
                .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired JWT token"));
    }

    @Test
    void verifyRefreshTokenTimedOut_ShouldReturn401() throws Exception {
        RefreshTokenService refreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtConfig, jwtUtilityService.getClock());
        Instant timeNow = jwtUtilityService.getClock().instant();
        Clock pastClock = Clock.fixed(timeNow.minusMillis(4 * jwtConfig.getRefreshExpiration()), ZoneOffset.UTC);
        RefreshTokenService expiredRefreshTokenService = new RefreshTokenService(refreshTokenRepository, jwtConfig, pastClock);
        User user = new User("testuser", "Password1");
        user = userRepository.save(user);
        RefreshToken expiredToken = expiredRefreshTokenService.createRefreshToken(user);

        assertFalse(refreshTokenService.isValidRefreshToken(expiredToken.getRefreshToken()));
        String requestBody = """
        {
          "refreshToken": "%s"
        }
        """.formatted(expiredToken.getRefreshToken());

        mockMvc.perform(post("/api/v1/auth/verifyrefresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"));
    }

    @Test
    void verifyRefreshTokenMissingBody_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verifyrefresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token is required"));
    }

    @Test
    void verifyAccessTokenMissingBody_ShouldReturn400() throws Exception { // handled by filter
        mockMvc.perform(post("/api/v1/auth/verifyaccess")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Access token is required"));
    }
}
