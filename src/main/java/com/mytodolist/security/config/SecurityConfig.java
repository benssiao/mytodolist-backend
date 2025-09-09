package com.mytodolist.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.mytodolist.security.filters.JwtAuthFilter;
import com.mytodolist.security.providers.UsernamePasswordAuthenticationProvider;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsernamePasswordAuthenticationProvider authenticationProvider;
    private final JwtAuthFilter JwtAuthFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint();

    public SecurityConfig(UsernamePasswordAuthenticationProvider authenticationProvider, JwtAuthFilter JwtAuthFilter) {
        this.authenticationProvider = authenticationProvider;
        this.JwtAuthFilter = JwtAuthFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        );
        http
                .sessionManagement(session
                        -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/login").permitAll()
                .anyRequest().authenticated()
                )
                .addFilterBefore(JwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable()); // Disable CSRF for stateless JWT

        return http.build();
    }

}
