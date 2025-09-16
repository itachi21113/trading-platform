package com.trading.price_streamer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Define which requests to protect
                .authorizeHttpRequests(authorize -> authorize
                        // Allow the WebSocket endpoint to be accessed by anyone
                        .requestMatchers("/ws/**").permitAll()
                        // Any other request must be authenticated (i.e., have a valid token)
                        .anyRequest().authenticated()
                )
                // Enable OAuth2 Resource Server support with JWT validation
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {})
                );

        return http.build();
    }
}