package com.mytodolist.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import com.mytodolist.dto.UsernameAndPasswordDTO;
import com.mytodolist.dto.RefreshRequestDTO;
import com.mytodolist.dto.LogoutRequest;

import org.springframework.web.bind.annotation.CrossOrigin;

import com.mytodolist.dto.LoginResponseDTO;
import com.mytodolist.dto.RefreshResponseDTO;
import com.mytodolist.dto.RegisterResponseDTO;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import com.mytodolist.model.User;
import com.mytodolist.model.RefreshToken;
import com.mytodolist.service.RefreshTokenService;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.service.UserService;
import com.mytodolist.dto.RegisterResponseDTO;
import com.mytodolist.security.config.PasswordConfig;
import com.mytodolist.dto.LogoutRequestDTO;
import com.mytodolist.dto.RefreshRequestDTO;
import com.mytodolist.dto.LogoutResponseDTO;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtilityService jwtUtilityService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthenticationController(AuthenticationManager authenticationManager,
            JwtUtilityService jwtUtilityService, RefreshTokenService refreshTokenService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtilityService = jwtUtilityService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UsernameAndPasswordDTO authRequest) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        String username = auth.getName();
        TodoUserDetails userDetails = (TodoUserDetails) auth.getPrincipal();

        refreshTokenService.logout(userDetails.getUser()); // This deletes old tokens
        String jwtToken = jwtUtilityService.generateToken(auth.getName());
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(userDetails);

        LoginResponseDTO response = new LoginResponseDTO(jwtToken, refreshTokenEntity.getRefreshToken(), username, userDetails.getId());

        return ResponseEntity.status(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(response);

    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@RequestBody UsernameAndPasswordDTO authRequest) {
        User newUser = new User();
        newUser.setUsername(authRequest.getUsername());
        newUser.setPassword(authRequest.getPassword());
        User createdUser = userService.createUser(newUser);
        if (createdUser == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User registration failed");
        }
        RegisterResponseDTO response = new RegisterResponseDTO(
                "User registered successfully",
                createdUser.getId(),
                createdUser.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(@RequestBody RefreshRequestDTO refreshRequest) {
        String requestTokenString = refreshRequest.getOldRefreshToken();
        if (requestTokenString == null || requestTokenString.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        if (!refreshTokenService.isValidRefreshToken(requestTokenString)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }
        RefreshToken existingToken = refreshTokenService.findByToken(requestTokenString); // get the refresh token object
        User user = existingToken.getUser();
        String newAccessToken = jwtUtilityService.generateToken(user.getUsername()); // make a new access token

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user); // begin making new refresh token object. This constructor gives Id and create Time

        RefreshResponseDTO newRefreshTokenResponse = new RefreshResponseDTO(newAccessToken, newRefreshToken.getRefreshToken());

        refreshTokenService.invalidateRefreshToken(existingToken); // invalidate old refresh token

        return ResponseEntity.ok().body(newRefreshTokenResponse);

    }

    @PostMapping("/logout")
    public ResponseEntity<LogoutResponseDTO> logout(@RequestBody LogoutRequestDTO logoutRequest) {

        String username = logoutRequest.getUsername();
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required for logout");
        }
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        refreshTokenService.logout(user);

        return ResponseEntity.ok().body(new LogoutResponseDTO("User logged out successfully", username));

    }
