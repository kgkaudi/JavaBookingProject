package com.kostas.bookingproject.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

        http
            // ---------------------------------------------------------
            // CORE SECURITY SETTINGS
            // ---------------------------------------------------------
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ---------------------------------------------------------
            // AUTHORIZATION RULES
            // ---------------------------------------------------------
            .authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS (no JWT required)
                .requestMatchers(
                        "/api/auth/login",
                        "/api/auth/signup",
                        "/api/auth/request-reset",
                        "/api/auth/reset-password"
                ).permitAll()
                .requestMatchers("/api/rooms/price/**").permitAll()

                // USER PROFILE
                .requestMatchers(HttpMethod.GET, "/api/users/me")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/me")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // ROLE CHANGES: admin only (must be listed BEFORE the generic PUT rule)
                .requestMatchers(HttpMethod.PUT, "/api/users/*/promote", "/api/users/*/demote")
                    .hasAuthority("ROLE_ADMIN")

                // SELF/ADMIN PROFILE UPDATE: finer check done by @PreAuthorize on the controller
                .requestMatchers(HttpMethod.PUT, "/api/users/*")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // ADMIN USER MANAGEMENT
                .requestMatchers(HttpMethod.GET, "/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ROLE_ADMIN")

                // ROOMS
                .requestMatchers(HttpMethod.GET, "/api/rooms/**").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/rooms/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/rooms/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/rooms/**").hasAuthority("ROLE_ADMIN")

                // BOOKINGS
                .requestMatchers(HttpMethod.GET, "/api/bookings").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/availability")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/me")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/*")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/user/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings/room/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/bookings")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasAuthority("ROLE_ADMIN")

                // BOOKING UPDATE / CANCEL (service layer enforces ownership/admin)
                .requestMatchers(HttpMethod.PUT, "/api/bookings/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/bookings/*/cancel")
                    .hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                // EVERYTHING ELSE
                .anyRequest().authenticated()
            )

            // ---------------------------------------------------------
            // FILTER ORDER
            // ---------------------------------------------------------
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ---------------------------------------------------------
    // CORS CONFIGURATION
    // ---------------------------------------------------------
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:http://localhost:5173}") List<String> allowedOrigins) {

        CorsConfiguration config = new CorsConfiguration();

        // Origins come from config (comma separated) so production can set its real frontend URL
        config.setAllowedOrigins(allowedOrigins);

        // PATCH is required for "cancel booking"; without it browsers block the preflight request
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ---------------------------------------------------------
    // AUTHENTICATION MANAGER + PASSWORD ENCODER
    // ---------------------------------------------------------
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
