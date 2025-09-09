package com.mytodolist.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.mytodolist.model.RefreshToken;
import com.mytodolist.model.User;
import com.mytodolist.repository.RefreshTokenRepository;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.userdetails.TodoUserDetails;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository, JwtConfig jwtConfig) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtConfig = jwtConfig;
    }

    //CREATE
    public RefreshToken createRefreshToken(User user) {
        // create new token based on user.
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        // milliseconds to LocalDateTime
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshExpiration() / 1000);
        refreshToken.setExpiresAt(expiresAt);

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
        return refreshTokenRepository.findValidRefreshToken(token, LocalDateTime.now()).isPresent();
    }

}
