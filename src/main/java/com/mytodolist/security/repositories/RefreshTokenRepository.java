package com.mytodolist.security.repositories;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mytodolist.models.User;
import com.mytodolist.security.models.RefreshToken;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    List<RefreshToken> findByUser(User user);

    List<RefreshToken> findByUserId(Long userId);

    //Logout functionality
    void deleteByUser(User user);

    void deleteByUserId(Long userId);

    //Cleaning up expired tokens
    @Modifying
    @Query("DELETE from RefreshToken rt where rt.expiresAt < :now")
    void deleteExpiredToken(@Param("now") Instant now);

    // find tokens by user AND expires before :now
    List<RefreshToken> findByUserAndExpiresAtBefore(User user, Instant now);

    // check if token exists and is not expired
    @Query("SELECT rt from RefreshToken rt WHERE rt.refreshToken = :token AND rt.expiresAt > :now")
    Optional<RefreshToken> findValidRefreshToken(@Param("token") String token, @Param("now") Instant now);

}
