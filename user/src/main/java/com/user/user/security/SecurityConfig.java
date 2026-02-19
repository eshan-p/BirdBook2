package com.user.user.security;

import java.util.List;

import com.user.user.repository.UserDAO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                // Preflight
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // PUBLIC - Auth endpoints
                .requestMatchers("/auth/**").permitAll()
                
                // PUBLIC - User registration
                .requestMatchers(HttpMethod.POST, "/users").permitAll()

                // PUBLIC - Read-only user lookups for inter-service calls
                .requestMatchers(HttpMethod.GET, "/users/**").permitAll()

                // PUBLIC - Static media
                .requestMatchers(HttpMethod.GET, "/images/**", "/profile_pictures/**", "/backend_profile_pictures/**").permitAll()

                // PUBLIC - Internal service callback used by group-service
                .requestMatchers(HttpMethod.PUT, "/users/*/groups/*").permitAll()

                // PUBLIC - Internal service callback used by post-service
                .requestMatchers(HttpMethod.PUT, "/users/*/posts/*").permitAll()

                // AUTHENTICATED - Everything else in User Service
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserDAO userDAO) {
        return username -> userDAO.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole().name())
                        .build()
                )
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + username)
                );
    }
}
