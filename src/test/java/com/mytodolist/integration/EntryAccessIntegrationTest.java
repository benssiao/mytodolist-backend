package com.mytodolist.integration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class EntryAccessIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @TestConfiguration
    static class FixedClockConfig {

        @Bean
        Clock testClock() {
            // Fixed instant for reproducible tests
            return Clock.fixed(Instant.parse("2025-01-01T00:00:00Z"), ZoneOffset.UTC);
        }
    }
    @Autowired
    Clock clock; 

    @Test
    void accessWithoutToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/entries"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User is not authenticated"));
    }

    @Test
    void accessWithInvalidToken_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/entries").header("Authorization", "Bearer faketoken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired JWT token"));
    }
}
