package com.mytodolist.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.mytodolist.models.User;
import com.mytodolist.security.models.RefreshToken;
import com.mytodolist.security.repositories.RefreshTokenRepository;
import com.mytodolist.security.services.RefreshTokenService;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RefreshTokenServiceIntegrationTest {

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private com.mytodolist.repositories.UserRepository userRepository;

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // Override the application's Clock bean with a controllable mock
    @MockBean
    private Clock clock;

    private final Instant fixedInstant = Instant.parse("2025-01-01T00:00:00Z");

    @BeforeEach
    void setup() {
        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        refreshTokenRepository.deleteAll(); // keep DB clean
    }

    private User createValidUser(String username) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("StrongPass123!"));
        return userRepository.save(user);
    }

    @Test
    void createRefreshToken_ShouldPersistAndSetCorrectTimestamps() {
        // Given
        User user = createValidUser("refresher123");

        // When
        RefreshToken token = refreshTokenService.createRefreshToken(user);

        // Then
        assertThat(token).isNotNull();
        assertThat(token.getUser()).isEqualTo(user);
        assertThat(token.getCreatedAt()).isEqualTo(fixedInstant);
        assertThat(token.getExpiresAt()).isAfter(fixedInstant);

        // And verify it’s actually persisted
        RefreshToken found = refreshTokenRepository.findById(token.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getRefreshToken()).isEqualTo(token.getRefreshToken());
    }

    @Test
    void isValidRefreshToken_ShouldReturnFalse_WhenExpired() {
        // Given
        User user = createValidUser("refresher123");
        RefreshToken token = refreshTokenService.createRefreshToken(user);
        String refreshToken = token.getRefreshToken();

        // Simulate time passing
        Instant expiredInstant = token.getExpiresAt().plusSeconds(1);
        when(clock.instant()).thenReturn(expiredInstant);

        // When
        boolean valid = refreshTokenService.isValidRefreshToken(refreshToken);

        // Then
        assertThat(valid).isFalse();
    }

    @Test
    void refreshToken_ShouldUpdateExistingTokenSuccessfully() {
        User user = createValidUser("refresher123");
        RefreshToken oldToken = refreshTokenService.createRefreshToken(user);
        RefreshToken newToken = refreshTokenService.createRefreshToken(user);

        RefreshToken updated = refreshTokenService.refreshToken(oldToken, newToken);

        assertThat(updated.getRefreshToken()).isEqualTo(newToken.getRefreshToken());
        assertThat(updated.getExpiresAt()).isEqualTo(newToken.getExpiresAt());
    }

    @Test
    void deleteExpiredTokens_ShouldRemoveOldTokens() {
        User user = createValidUser("cleanupuser");
        RefreshToken token = refreshTokenService.createRefreshToken(user);

        when(clock.instant()).thenReturn(token.getExpiresAt().plusSeconds(1));
        refreshTokenService.deleteExpiredTokens();

        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    @Test
    void createRefreshToken_ShouldThrow_WhenUserInvalid() {
        User invalidUser = new User();
        invalidUser.setUsername("ab"); // too short
        invalidUser.setPassword("123"); // too weak

        // We can simulate your validation layer, or just expect JPA to fail
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(invalidUser))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("transient"); // unsaved invalid user reference
    }
}
