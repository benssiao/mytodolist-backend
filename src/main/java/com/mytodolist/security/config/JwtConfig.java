package com.mytodolist.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@ConfigurationProperties(prefix = "jwt")
@Validated
@Component
public class JwtConfig {

    @NotBlank(message = "JWT secret cannot be blank")
    private String secret;
    @NotBlank(message = "JWT refresh secret cannot be blank")
    private String refreshSecret;

    @NotNull(message = "JWT expiration must be set")
    @Min(value = 1, message = "Expiration must be positive")
    Long expiration;
    @NotNull(message = "JWT refresh expiration must be set")
    @Min(value = 1, message = "Refresh expiration must be positive")
    Long refreshExpiration;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Long getExpiration() {
        return expiration;
    }

    public void setExpiration(Long expiration) {
        this.expiration = expiration;
    }

    public String getRefreshSecret() {
        return refreshSecret;
    }

    public void setRefreshSecret(String refreshSecret) {
        this.refreshSecret = refreshSecret;
    }

    public Long getRefreshExpiration() {
        return refreshExpiration;
    }

    public void setRefreshExpiration(Long refreshExpiration) {
        this.refreshExpiration = refreshExpiration;
    }

}
