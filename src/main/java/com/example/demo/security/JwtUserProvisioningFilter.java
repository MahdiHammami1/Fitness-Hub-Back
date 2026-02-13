package com.example.demo.security;

import com.example.demo.model.User;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Component
public class JwtUserProvisioningFilter extends OncePerRequestFilter {

    private final UserService userService;

    public JwtUserProvisioningFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) auth.getPrincipal();
                String email = jwt.getClaimAsString("email");
                if (email != null && !userService.existsByEmail(email)) {
                    // Create a minimal user record. Mark as enabled = true (verified by default).
                    User u = User.builder()
                            .email(email)
                            .fullName(jwt.getClaimAsString("name"))
                            .role(Role.CUSTOMER)
                            .enabled(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    userService.create(u);
                } else if (email != null) {
                    // Optional: ensure existing users are enabled when the identity comes from external provider
                    userService.findByEmail(email).ifPresent(existing -> {
                        if (existing.getEnabled() == null || !existing.getEnabled()) {
                            existing.setEnabled(true);
                            existing.setUpdatedAt(Instant.now());
                            userService.update(existing.getId(), existing);
                        }
                    });
                }
            }
        } catch (Exception ex) {
            // don't block the request on provisioning errors; just log via stderr for now
            System.err.println("JwtUserProvisioningFilter error: " + ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

