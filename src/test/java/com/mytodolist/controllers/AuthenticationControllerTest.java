package com.mytodolist.controllers;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytodolist.exceptions.AuthenticationFailedException;
import com.mytodolist.exceptions.DuplicateUsernameException;
import com.mytodolist.models.User;
import com.mytodolist.security.config.JwtAuthenticationEntryPoint;
import com.mytodolist.security.config.JwtConfig;
import com.mytodolist.security.config.TimeConfig;
import com.mytodolist.security.controllers.AuthenticationController;
import com.mytodolist.security.dtos.LogoutRequestDTO;
import com.mytodolist.security.dtos.RefreshRequestDTO;
import com.mytodolist.security.dtos.UsernameAndPasswordDTO;
import com.mytodolist.security.dtos.VerifyAccessTokenRequestDTO;
import com.mytodolist.security.dtos.VerifyRefreshTokenDTO;
import com.mytodolist.security.filters.JwtAuthFilter;
import com.mytodolist.security.models.RefreshToken;
import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.services.RefreshTokenService;
import com.mytodolist.security.services.RoleService;
import com.mytodolist.security.userdetails.TodoUserDetails;
import com.mytodolist.security.userdetails.TodoUserDetailsService;
import com.mytodolist.services.UserService;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TimeConfig.class)
public class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtUtilityService jwtUtilityService;
    @MockBean
    RefreshTokenService refreshTokenService;
    @MockBean
    UserService userService;
    @MockBean
    RoleService roleService;
    @MockBean
    AuthenticationManager authenticationManager;
    @MockBean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    @MockBean
    JwtAuthFilter jwtAuthFilter;
    @MockBean
    TodoUserDetailsService todoUserDetailsService;
    @MockBean
    JwtConfig jwtConfig; 

    

    // ---- LOGIN ----
    @Test
    public void testLogin_Success() throws Exception {
        UsernameAndPasswordDTO loginDTO = new UsernameAndPasswordDTO();
        loginDTO.setUsername("testuser");
        loginDTO.setPassword("password");

        User user = new User("testuser", "password");
        user.setId(1L);

        TodoUserDetails userDetails = new TodoUserDetails(user, roleService);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(roleService.getUserRoles(1L)).thenReturn(Set.of("USER"));
        when(jwtUtilityService.generateToken(userDetails)).thenReturn("mockJwt");
        when(refreshTokenService.createRefreshToken(any(TodoUserDetails.class)))
                .thenReturn(new RefreshToken(user, null, null));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.accessToken").value("mockJwt"))
                .andExpect(jsonPath("$.roles[0]").value("USER"))
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    public void testLogin_Failure_InvalidCredentials() throws Exception {
        UsernameAndPasswordDTO loginDTO = new UsernameAndPasswordDTO();
        loginDTO.setUsername("wronguser");
        loginDTO.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new AuthenticationFailedException("Invalid username or password"));

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testLogin_Failure_NoRolesAssigned() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("testuser");
        dto.setPassword("password");

        User user = new User("testuser", "password");
        user.setId(1L);
        TodoUserDetails userDetails = new TodoUserDetails(user, roleService);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(roleService.getUserRoles(1L)).thenReturn(Set.of()); // no roles

        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("User has no roles assigned"))
                .andExpect(jsonPath("$.details").exists());
    }

    // ---- REGISTER ----
    @Test
    public void testRegister_Success() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("newuser");
        dto.setPassword("Password123");

        User createdUser = new User("newuser", "Password123");
        createdUser.setId(10L);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    public void testRegister_Failure_DuplicateUsername() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("existinguser");
        dto.setPassword("Password123");

        when(userService.createUser(any(User.class))).thenThrow(new DuplicateUsernameException("Username already exists"));

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    public void testRegister_Failure_InvalidInput_UsernameShort() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("below"); // min size is 6
        dto.setPassword("Password123"); // Invalid password
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("username: Username must be at least 6 characters")));
    }

    @Test
    public void testRegister_Failure_InvalidInput_BlankUsername() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername(""); // blank
        dto.setPassword("Password123"); // Invalid password
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", allOf(
                        containsString("Username can only contain letters, numbers, hyphens, and underscores"),
                        containsString("Username must be at least 6 characters")
                )));
    }

    @Test
    public void testRegister_Failure_InvalidInput_PasswordWeak() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("validuser");
        dto.setPassword("weak"); // Invalid password
        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Password must contain at least one uppercase letter, one lowercase letter, and one digit")));
    }
    // ---- REFRESH ----

    @Test
    public void testRefresh_Success() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("oldToken");

        User user = new User("testuser", "pass");
        user.setId(1L);

        RefreshToken old = new RefreshToken(user, null, null);
        RefreshToken newToken = new RefreshToken(user, null, null);

        when(refreshTokenService.isValidRefreshToken("oldToken")).thenReturn(true);
        when(refreshTokenService.findByToken("oldToken")).thenReturn(old);
        when(jwtUtilityService.generateToken(user)).thenReturn("newAccessToken");
        when(refreshTokenService.createRefreshToken(user)).thenReturn(newToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("newAccessToken"));
    }

    @Test
    public void testRefresh_Failure_InvalidToken() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken("invalidToken");

        when(refreshTokenService.isValidRefreshToken("invalidToken")).thenReturn(false);
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }

    @Test
    public void testRefresh_Failure_MissingToken() throws Exception {
        RefreshRequestDTO dto = new RefreshRequestDTO();
        dto.setRefreshToken(""); // blank token

        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Refresh token is required"));
    }

    // ---- LOGOUT ----
    @Test
    public void testLogout_Success() throws Exception {
        LogoutRequestDTO dto = new LogoutRequestDTO("testuser");
        User user = new User("testuser", "pass");
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testLogout_Failure_UserNotFound() throws Exception {
        LogoutRequestDTO dto = new LogoutRequestDTO("missinguser");
        when(userService.findByUsername("missinguser")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with username: missinguser"))
                .andExpect(jsonPath("$.details").exists());
    }

    // ---- VERIFY ACCESS ----
    @Test
    public void testVerifyAccess_ValidToken() throws Exception {
        VerifyAccessTokenRequestDTO dto = new VerifyAccessTokenRequestDTO();
        dto.setAccessToken("validToken");
        when(jwtUtilityService.validateToken("validToken")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/verifyaccess")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Access token is valid"));
    }

    @Test
    public void testVerifyAccess_InvalidToken() throws Exception {
        VerifyAccessTokenRequestDTO dto = new VerifyAccessTokenRequestDTO();
        dto.setAccessToken("invalidToken");
        when(jwtUtilityService.validateToken("invalidToken")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/verifyaccess")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid or expired access token"))
                .andExpect(jsonPath("$.details").exists());
    }

    // --- VERIFY REFRESH ---
    @Test
    public void testVerifyRefresh_ValidToken() throws Exception {

        VerifyRefreshTokenDTO dto = new VerifyRefreshTokenDTO();
        dto.setRefreshToken("validToken");
        when(refreshTokenService.isValidRefreshToken("validToken")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/verifyrefresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Refresh token is valid"));
    }

    @Test
    public void testVerifyRefresh_InvalidToken() throws Exception {
        VerifyRefreshTokenDTO dto = new VerifyRefreshTokenDTO();
        dto.setRefreshToken("invalidRefresh");
        when(refreshTokenService.isValidRefreshToken("invalidRefresh")).thenReturn(false);

        mockMvc.perform(post("/api/v1/auth/verifyrefresh")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid or expired refresh token"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    public void testRegister_VerifyRoleAssignment() throws Exception {
        UsernameAndPasswordDTO dto = new UsernameAndPasswordDTO();
        dto.setUsername("brandnew");
        dto.setPassword("Password123");

        User createdUser = new User("brandnew", "Password123");
        createdUser.setId(100L);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/v1/auth/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("brandnew"));

        // ✅ Verify the controller assigned a default USER role
        verify(roleService).assignRoleToUser(100L, "ROLE_USER");
    }

}
