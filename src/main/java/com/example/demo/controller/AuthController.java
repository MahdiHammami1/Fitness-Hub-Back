package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
     public ResponseEntity<AuthResponse> signup(@RequestBody SignUpRequest req) {
        String token = authService.signUp(req.getEmail(), req.getPassword(), req.getFullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthResponse> signin(@RequestBody SignInRequest req) {
        String token = authService.signIn(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
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
    public ResponseEntity<UserResponse> getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = authService.getLoggedInUser(email);
        return ResponseEntity.ok(new UserResponse(user));
    }

    // ...existing code...
    public static class SignUpRequest {
        private String email;
        private String password;
        private String fullName;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
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
