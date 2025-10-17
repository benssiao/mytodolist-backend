package com.mytodolist.security.services;

import java.time.Clock;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.models.RefreshToken;
import com.mytodolist.security.repositories.RefreshTokenRepository;
import com.mytodolist.security.userdetails.TodoUserDetails;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;
    private final Clock clock;


    

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtConfig jwtConfig, Clock clock) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtConfig = jwtConfig;
        this.clock = clock;
    }

    //CREATE
    public RefreshToken createRefreshToken(User user) {
        // create new token based on user.
        Instant now = Instant.now(clock);
        Instant expiry = now.plusSeconds(jwtConfig.getRefreshExpiration() / 1000);
        RefreshToken refreshToken = new RefreshToken(user, now, expiry);

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken createRefreshToken(TodoUserDetails todoUserDetails) {
        return this.createRefreshToken(todoUserDetails.getUser());
    }

    //READ
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByRefreshToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
    }

    //DELETE
    public void logout(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public void invalidateRefreshToken(RefreshToken token) {
        if (token != null) {
            refreshTokenRepository.delete(token);
        }
    }

    public RefreshToken refreshToken(RefreshToken existingToken, RefreshToken newToken) {
        if (existingToken == null || newToken == null) {
            throw new IllegalArgumentException("Tokens cannot be null");
        }
        existingToken.setRefreshToken(newToken.getRefreshToken());
        existingToken.setExpiresAt(newToken.getExpiresAt());
        return refreshTokenRepository.save(existingToken);
    }

    public boolean isValidRefreshToken(String token) {
        return refreshTokenRepository.findValidRefreshToken(token, Instant.now(clock)).isPresent(); // this tries to find any refreshtoken which is not expired
        // with comparison to LocalDateTime.now().
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteExpiredToken(Instant.now(clock));
    }

}
