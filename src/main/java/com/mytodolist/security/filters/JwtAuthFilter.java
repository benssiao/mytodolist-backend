package com.mytodolist.security.filters;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.mytodolist.security.services.JwtUtilityService;
import com.mytodolist.security.userdetails.TodoUserDetailsService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtilityService jwtService;
    private final TodoUserDetailsService todoUserDetailsService;

    public JwtAuthFilter(JwtUtilityService jwtService, TodoUserDetailsService todoUserDetailsService) {
        this.jwtService = jwtService;
        this.todoUserDetailsService = todoUserDetailsService;
    }

    @Override
    public void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = authHeader.substring(7);
        log.debug("JWT Token: {}", jwt);
        if (!jwtService.validateToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.warn("Invalid JWT token");

            return;
        }
        String username = jwtService.getUsernameFromToken(jwt);
        log.debug("Username from token: {}", username);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = todoUserDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
                log.debug("User {} authenticated successfully", username);

            } catch (Exception e) {
                log.error("Error during authentication: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                return;
            }

        }
        filterChain.doFilter(request, response);

    }
}
