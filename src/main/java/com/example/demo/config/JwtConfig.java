package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class JwtConfig {

    @Value("${keycloak.issuer:http://localhost:8081/realms/wouhouch}")
    private String keycloakIssuer;

    /**
     * Lazy-loaded JwtDecoder for Keycloak.
     * Returns a dummy decoder if Keycloak is not available (will be handled by HybridJwtDecoder)
     */
    @Bean(name = "keycloakDecoder")
    public JwtDecoder keycloakDecoder() {
        try {
            log.info("Attempting to create Keycloak JWT decoder for issuer: {}", keycloakIssuer);
            return JwtDecoders.fromIssuerLocation(keycloakIssuer);
        } catch (Exception e) {
            log.warn("Failed to create Keycloak JWT decoder. Will use local JWT decoder only. Error: {}", e.getMessage());
            // Return a dummy decoder that always fails - HybridJwtDecoder will handle this
            return token -> {
                throw new org.springframework.security.oauth2.jwt.BadJwtException("Keycloak decoder not available");
            };
        }
    }

    /**
     * Primary JwtDecoder bean that supports both local and Keycloak tokens.
     * Uses HybridJwtDecoder from the service package.
     */
    @Bean
    public JwtDecoder jwtDecoder(org.springframework.context.ApplicationContext context) {
        // Get the HybridJwtDecoder component
        return context.getBean(com.example.demo.service.HybridJwtDecoder.class);
    }

}

