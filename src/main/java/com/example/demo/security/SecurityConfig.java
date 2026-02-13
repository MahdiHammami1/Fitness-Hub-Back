// java
package com.example.demo.security;

import com.example.demo.service.HybridJwtDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.config.http.SessionCreationPolicy;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final HybridJwtDecoder hybridJwtDecoder;

    public SecurityConfig(HybridJwtDecoder hybridJwtDecoder) {
        this.hybridJwtDecoder = hybridJwtDecoder;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authEndpointFilterChain(HttpSecurity http) throws Exception {
        // This security chain applies only to /api/auth/** and should permit all requests
        http
                .securityMatcher(request -> request.getRequestURI().startsWith("/api/auth/"))
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Do NOT configure oauth2ResourceServer here so invalid tokens won't block controller logic
                ;
        return http.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // CORS en PREMIER
                .cors(Customizer.withDefaults())

                // API stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Ne pas créer de session (stateless API)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Gérer les erreurs d'authentification en renvoyant 401 (au lieu d'une redirection 302 vers une page de login)
                .exceptionHandling(eh -> eh.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))

                // Configuration des autorisations
                .authorizeHttpRequests(auth -> auth
                        // Autoriser preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints publics/static/docs
                        .requestMatchers(

                                "/api/auth/**"
                        ).permitAll()

                        // Protéger TOUTE l'API
                        .requestMatchers("/api/**").authenticated()

                        // Le reste est autorisé (utile pour assets, landing pages, etc.)
                        .anyRequest().permitAll()
                )

                // Configurer comme resource server JWT avec HybridJwtDecoder (accepte local + Keycloak)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(hybridJwtDecoder)));

        return http.build();
    }

    // CORS config (mise à jour)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Autoriser explicitement l'origine du front (ne pas utiliser "*" si allowCredentials=true)
        config.setAllowedOriginPatterns(List.of("http://localhost:3001"));

        // Méthodes autorisées
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));

        // Autoriser les en-têtes envoyés par le front (Authorization est critique)
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));

        // Exposer l'en-tête Authorization si nécessaire côté client
        config.setExposedHeaders(List.of("Authorization"));

        // Autoriser l'envoi de credentials si le front les utilise (cookies, etc.)
        config.setAllowCredentials(true);

        // Cache des preflight
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
