package com.example.studentmanagement.config;

import com.example.studentmanagement.security.CustomUserDetailsService;
import com.example.studentmanagement.security.JwtAuthFilter;
import com.example.studentmanagement.security.JwtAuthenticationEntryPoint;
import com.example.studentmanagement.security.JwtUtil;
import com.example.studentmanagement.service.TokenBlacklistService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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

    // CORS: the frontend runs on its own port (e.g. 5500), so the browser needs
   
    // this backend to say "that origin is allowed to call me".
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));   // includes the Authorization header

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtUtil jwtUtil,
                                           CustomUserDetailsService userDetailsService,
                                           TokenBlacklistService blacklistService) throws Exception {

        JwtAuthFilter jwtFilter = new JwtAuthFilter(jwtUtil, userDetailsService, blacklistService);

        http
            .cors(Customizer.withDefaults())  // lets the separate frontend call this API (see bean below)
            .csrf(csrf -> csrf.disable())   // JWT in a header, no cookies/sessions
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(new JwtAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                // Spring's own error page
                .requestMatchers("/error", "/api/health").permitAll()
                // register, login, logout
                .requestMatchers("/api/auth/**").permitAll()
                // everything else (all /api/students/**) needs a valid token
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
