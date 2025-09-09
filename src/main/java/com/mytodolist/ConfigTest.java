package com.mytodolist;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class ConfigTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @PostConstruct
    public void testConfig() {
        System.out.println("JWT Secret loaded: " + jwtSecret);
        if ("default-secret-key".equals(jwtSecret)) {
            System.out.println("WARNING: Using default JWT secret! Check your .env file.");
        }
    }
}
