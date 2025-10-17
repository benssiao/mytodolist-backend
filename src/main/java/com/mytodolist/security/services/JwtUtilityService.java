package com.mytodolist.security.services;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey; // Add this import

import org.springframework.stereotype.Service;

import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.userdetails.TodoUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Service
public class JwtUtilityService {

    private final JwtConfig jwtConfig;
    private final SecretKey signingKey;
    private final Clock clock;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JwtUtilityService.class);

    public JwtUtilityService(JwtConfig jwtConfig, Clock clock) {
        this.jwtConfig = jwtConfig;
        this.signingKey = Keys.hmacShaKeyFor(jwtConfig.getSecret().getBytes()); // This returns SecretKey
        this.clock = clock;
    }

    public String generateToken(User user) {
        String username = user.getUsername();
        Instant now = Instant.now(clock);
        Instant expiry = now.plusSeconds(jwtConfig.getExpiration() / 1000);

        return Jwts.builder()
                .subject(username)
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public String generateToken(TodoUserDetails todoUserDetails) {
        User user = todoUserDetails.getUser();
        return generateToken(user);
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token)
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return !isTokenExpired(token);
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            return expiration.toInstant().isBefore(Instant.now(clock));

        } catch (Exception e) {
            return true;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaims(token).getExpiration();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
