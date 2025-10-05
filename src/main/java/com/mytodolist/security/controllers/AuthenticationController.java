package com.mytodolist.security.controllers;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mytodolist.models.User;
import com.mytodolist.security.dtos.LoginResponseDTO;
import com.mytodolist.security.dtos.LogoutRequestDTO;
import com.mytodolist.security.dtos.LogoutResponseDTO;
import com.mytodolist.security.dtos.RefreshRequestDTO;
import com.mytodolist.security.dtos.RefreshResponseDTO;
import com.mytodolist.security.dtos.RegisterResponseDTO;
import com.mytodolist.security.dtos.UsernameAndPasswordDTO;
import com.mytodolist.security.dtos.VerifyAccessTokenRequestDTO;
import com.mytodolist.security.dtos.VerifyRefreshTokenDTO;
import com.mytodolist.security.models.RefreshToken;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.services.RefreshTokenService;
import com.mytodolist.security.services.RoleService;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.services.UserService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtilityService jwtUtilityService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final RoleService roleService;

    public AuthenticationController(AuthenticationManager authenticationManager,
            JwtUtilityService jwtUtilityService, RefreshTokenService refreshTokenService, UserService userService, RoleService roleService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtilityService = jwtUtilityService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
        this.roleService = roleService;

    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody UsernameAndPasswordDTO authRequest) {
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        String username = auth.getName();
        TodoUserDetails userDetails = (TodoUserDetails) auth.getPrincipal();

        Set<String> roles = roleService.getUserRoles(userDetails.getId());
        if (roles == null || roles.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User has no roles assigned");
        }
        refreshTokenService.logout(userDetails.getUser()); // This deletes old tokens

        String jwtToken = jwtUtilityService.generateToken(auth.getName());
        RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(userDetails);

        LoginResponseDTO response = new LoginResponseDTO(
                jwtToken,
                refreshTokenEntity.getRefreshToken(),
                username,
                userDetails.getId(),
                roles
        );

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

        roleService.assignRoleToUser(createdUser.getId(), "USER");
        RegisterResponseDTO response = new RegisterResponseDTO(
                "User registered successfully",
                createdUser.getId(),
                createdUser.getUsername()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshResponseDTO> refresh(@RequestBody RefreshRequestDTO refreshRequest) {
        String requestTokenString = refreshRequest.getRefreshToken();
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

    @PostMapping("/verifyaccess")
    public ResponseEntity<String> verifyToken(@RequestBody VerifyAccessTokenRequestDTO accessToken) {
        String accessTokenString = accessToken.getAccessToken();
        if (accessTokenString == null || accessTokenString.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        boolean isValid = jwtUtilityService.validateToken(accessTokenString);
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return ResponseEntity.ok("Token is valid");
    }

    @PostMapping("/verifyrefresh")
    public ResponseEntity<String> verifyRefreshToken(@RequestBody VerifyRefreshTokenDTO refreshToken) {
        String refreshTokenString = refreshToken.getRefreshToken();
        if (refreshTokenString == null || refreshTokenString.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refresh token is required");
        }
        boolean isValid = refreshTokenService.isValidRefreshToken(refreshTokenString);
        if (!isValid) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
        return ResponseEntity.ok("Refresh token is valid");
    }
}
