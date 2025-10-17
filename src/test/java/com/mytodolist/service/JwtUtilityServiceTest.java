package com.mytodolist.service;

import java.time.Clock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.lenient;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.services.JwtUtilityService;

@ExtendWith(MockitoExtension.class)

class JwtUtilityServiceTest {

    @Mock
    JwtConfig jwtConfig;
    @Mock
    Clock clock;

    JwtUtilityService jwtUtilityService;

    @BeforeEach
    void setup() {
        lenient().when(jwtConfig.getSecret()).thenReturn("supersecretkeysupersecretkey12345678");
        lenient().when(jwtConfig.getExpiration()).thenReturn(1000L);

        jwtUtilityService = new JwtUtilityService(jwtConfig, Clock.systemUTC());
    }

    @Test
    void testGenerateAndValidateToken() {
        User user = new User("testuser", "Password123");
        String token = jwtUtilityService.generateToken(user);
        assertTrue(jwtUtilityService.validateToken(token));
        assertEquals("testuser", jwtUtilityService.getUsernameFromToken(token));
    }

    @Test
    void testInvalidToken_Timeout() throws InterruptedException {
        User user = new User("testuser", "Password123");
        String token = jwtUtilityService.generateToken(user);
        Thread.sleep(1000);
        assertTrue(!jwtUtilityService.validateToken(token));
        assertTrue(token != null && !token.isEmpty());
    }

    @Test
    void testMalformedToken() {
        String malformedToken = "this.is.not.a.valid.token";
        assertTrue(!jwtUtilityService.validateToken(malformedToken));
    }

    @Test
    void testGetExpirationFromToken() {
        User user = new User("testuser", "Password123");
        String token = jwtUtilityService.generateToken(user);
        assertTrue(jwtUtilityService.getExpirationDateFromToken(token).after(new java.util.Date()));
    }
}
