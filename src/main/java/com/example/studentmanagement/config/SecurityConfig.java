package com.example.studentmanagement.config;

import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.security.JwtAuthFilter;
import com.example.studentmanagement.security.JwtAuthenticationEntryPoint;
import com.example.studentmanagement.security.JwtUtil;
import com.example.studentmanagement.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Used by AuthService.login() to check email + password
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtUtil jwtUtil,
                                           CustomUserDetailsService userDetailsService,
                                           TokenBlacklistService blacklistService) throws Exception {

        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtUtil, userDetailsService, blacklistService);

        http
            .csrf(csrf -> csrf.disable())   // JWT in a header, no cookies/sessions
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                // frontend files (the pages themselves are public; the DATA is protected)
                .requestMatchers("/", "/*.html", "/css/**", "/js/**", "/favicon.ico", "/error").permitAll()
                // register, login, logout
                .requestMatchers("/api/auth/**").permitAll()
                // everything else (all /api/students/**) needs a valid token
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
