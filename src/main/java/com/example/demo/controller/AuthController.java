package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import com.example.demo.service.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtDecoder jwtDecoder;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider, JwtDecoder jwtDecoder) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtDecoder = jwtDecoder;
    }

    // ...existing code...

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest req) {
        try {
            // signUp returns the email (String), not the User
            String email = authService.signUp(req.getEmail(), req.getPassword(), req.getFullName());

            // Fetch the user to get their ID for token generation
            User user = authService.getLoggedInUser(email);

            // Generate JWT token for the new user (requires email and userId)
            String token = jwtTokenProvider.generateToken(email, user.getId());
            long expirationMs = jwtTokenProvider.getJwtExpirationMs();

            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "expirationTime", expirationMs,
                    "authenticated", true,
                    "user", new UserResponse(user)
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SignInRequest req) {
        try {
            String token = authService.signIn(req.getEmail(), req.getPassword());
            long expirationMs = jwtTokenProvider.getJwtExpirationMs();
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "expirationTime", expirationMs,
                    "authenticated", true
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody CodeRequest req) {
        authService.verify(req.getCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody EmailRequest req) {
        authService.forgotPassword(req.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<?> reset(@RequestBody ResetRequest req) {
        authService.resetPassword(req.getCode(), req.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedInUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        // helper to write debug log to file
        Runnable appendLog = () -> {
            try {
                Files.createDirectories(Path.of("tmp"));
            } catch (IOException e) {
                // ignore
            }
        };

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // Selon la configuration de Security, authentication.getName() peut contenir l'email ou l'username
            email = authentication.getName();
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] SecurityContext provided name=").append(String.valueOf(email)).append("\n"); } catch (IOException ignored) {}
        }

        // Si on n'a pas d'email dans le SecurityContext, essayer d'extraire depuis le header Authorization (JWT Bearer)
        if ((email == null || email.isBlank()) && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String extracted = extractEmailFromToken(token);
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] extracted from token=").append(String.valueOf(extracted)).append("\n"); } catch (IOException ignored) {}
            if (extracted != null && !extracted.isBlank()) {
                email = extracted;
            }
        }

        if (email == null || email.isBlank()) {
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] No email found, returning unauthenticated response\n"); } catch (IOException ignored) {}
            // Return predictable body when not authenticated to avoid frontend refresh loop
            return ResponseEntity.ok(new UnauthenticatedResponse(false));
        }

        try {
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] Looking up user with key='").append(String.valueOf(email)).append("'\n"); } catch (IOException ignored) {}
            User user = authService.getLoggedInUser(email);
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] user found id=").append(String.valueOf(user != null ? user.getId() : null)).append("\n"); } catch (IOException ignored) {}
            return ResponseEntity.ok(new UserResponse(user));
        } catch (IllegalArgumentException ex) {
            // User not found in local DB: return 404 with a JSON error message instead of plain text
            try (FileWriter fw = new FileWriter("tmp/auth-me.log", true)) { fw.append("[AuthController] user not found for key='").append(String.valueOf(email)).append("' : ").append(ex.getMessage()).append("\n"); } catch (IOException ignored) {}
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
    }

    @GetMapping("/token-info")
    public ResponseEntity<?> tokenInfo(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing Authorization Bearer token"));
        }
        String token = authHeader.substring(7);
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String email = jwt.getClaimAsString("email");
            String preferred = jwt.getClaimAsString("preferred_username");
            String sub = jwt.getSubject();
            return ResponseEntity.ok(Map.of(
                    "email", email,
                    "preferred_username", preferred,
                    "sub", sub,
                    "allClaims", jwt.getClaims()
            ));
        } catch (Exception ex) {
            // fallback: decode payload without verification
            try {
                String[] parts = token.split("\\.");
                if (parts.length < 2) return ResponseEntity.badRequest().body(Map.of("error","Invalid token"));
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                JsonNode node = objectMapper.readTree(payloadJson);
                return ResponseEntity.ok(Map.of(
                        "payload", node
                ));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to decode token"));
            }
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> listLocalUsers() {
        var users = authService.listAllUsersShort();
        return ResponseEntity.ok(users);
    }

    private String extractEmailFromToken(String token) {
        // Utiliser JwtDecoder (vérifie signature, exp, etc.)
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String email = jwt.getClaimAsString("email");
            if (email == null || email.isBlank()) email = jwt.getClaimAsString("preferred_username");
            if (email == null || email.isBlank()) email = jwt.getSubject();
            return email;
        } catch (JwtException ex) {
            // si decode échoue, fallback léger en décodant la payload sans vérification
            try {
                String[] parts = token.split("\\.");
                if (parts.length < 2) return null;
                String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
                JsonNode node = objectMapper.readTree(payloadJson);
                if (node.hasNonNull("email")) return node.get("email").asText();
                if (node.hasNonNull("preferred_username")) return node.get("preferred_username").asText();
                if (node.hasNonNull("sub")) return node.get("sub").asText();
                if (node.hasNonNull("name")) return node.get("name").asText();
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    // DTOs
    public static class SignUpRequest {
        private String email;
        private String password;
        private String fullName;
        private String authProvider; // "local", "google", etc.

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getAuthProvider() { return authProvider; }
        public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    }

    public static class SignInRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class CodeRequest {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class EmailRequest {
        private String email;
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    public static class ResetRequest {
        private String code;
        private String newPassword;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }

    public static class AuthResponse {
        private String token;
        public AuthResponse() {}
        public AuthResponse(String token) { this.token = token; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
    }

    public static class UnauthenticatedResponse {
        private boolean authenticated;
        public UnauthenticatedResponse(boolean authenticated) { this.authenticated = authenticated; }
        public boolean isAuthenticated() { return authenticated; }
        public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }
    }

    public static class UserResponse {
        private String id;
        private String email;
        private String fullName;
        private String phone;
        private String role;
        private Boolean enabled;
        private Long createdAt;
        private Long updatedAt;

        public UserResponse() {}

        public UserResponse(User user) {
            if (user == null) return;
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.phone = user.getPhone();
            this.role = user.getRole() != null ? user.getRole().name() : null;
            this.enabled = user.getEnabled();
            this.createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : null;
            this.updatedAt = user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochMilli() : null;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
        public Long getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(Long updatedAt) { this.updatedAt = updatedAt; }
    }
}
