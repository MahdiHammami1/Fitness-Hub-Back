package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.VerificationToken;
import com.example.demo.model.TokenType;
import com.example.demo.repository.VerificationTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private final UserService userService;
    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // reuse Random
    private static final Random RANDOM = new Random();

    public AuthService(UserService userService,
                       VerificationTokenRepository tokenRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Create a local user record. When authentication is managed externally (Keycloak),
     * we do not issue JWTs here. This method returns the created user's email as a simple acknowledgement.
     *
     * @param authProvider "local", "google", etc. If "google", no verification code is sent (email already verified by Google).
     */
    public String signUp(String email, String password, String fullName, String authProvider) {
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already used");
        }

        // Default to "local" if not specified
        String provider = (authProvider != null && !authProvider.isBlank()) ? authProvider : "local";

        User u = User.builder()
                .email(email)
                // still store a password hash if provided, otherwise null
                .passwordHash(password == null ? null : passwordEncoder.encode(password))
                .fullName(fullName)
                .role(Role.CUSTOMER)
                .enabled(true) // enabled because external IdP handles authentication/verification
                .authProvider(provider)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        User created = userService.create(u);

        // Only send verification code if not using OAuth (Google, etc.) since they verify email themselves
        if (!"google".equalsIgnoreCase(provider)) {
            String code = generateCode();
            VerificationToken token = VerificationToken.builder()
                    .userId(created.getId())
                    .code(code)
                    .type(TokenType.VERIFICATION)
                    .expiresAt(Instant.now().plusSeconds(60L * 60 * 24))
                    .build();
            tokenRepository.save(token);

            // Use HTML template instead of plain text
            try {
                String html = EmailTemplates.emailVerification(fullName, code);
                emailService.sendHtmlMessage(email, "Vérification de votre compte", html);
            } catch (Exception ex) {
                // Log but don't throw - email failure shouldn't block signup
                System.err.println("Failed to send verification email: " + ex.getMessage());
            }
        }

        // Since external auth (Keycloak) issues JWT, we don't generate one here.
        return created.getEmail();
    }

    /**
     * Backward compatibility: call signUp with authProvider = "local"
     */
    public String signUp(String email, String password, String fullName) {
        return signUp(email, password, fullName, "local");
    }

    /**
     * Sign in with local credentials (email + password).
     * Generates and returns a JWT token for the authenticated user.
     *
     * @param email User email
     * @param password User password (plaintext, will be compared against passwordHash)
     * @return JWT token string
     * @throws IllegalArgumentException if credentials are invalid
     */
    public String signIn(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();

        // Check if user has a password hash (local authentication)
        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("This account uses external authentication (Google, Keycloak, etc.)");
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        // Check if user is enabled
        if (user.getEnabled() == null || !user.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled. Please verify your email.");
        }

        // Authentication successful - generate JWT token
        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getId());
        return token;
    }

    public void verify(String code) {
        Optional<VerificationToken> ot = tokenRepository.findByCodeAndType(code, TokenType.VERIFICATION);
        VerificationToken token = ot.orElseThrow(() -> new IllegalArgumentException("Invalid code"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Code expired");
        }
        String userId = token.getUserId();
        userService.findById(userId).ifPresent(user -> {
            user.setEnabled(true);
            user.setUpdatedAt(Instant.now());
            userService.update(user.getId(), user);
        });
        tokenRepository.delete(token);
    }

    public void forgotPassword(String email) {
        userService.findByEmail(email).ifPresent(user -> {
            String code = generateCode();
            VerificationToken token = VerificationToken.builder()
                    .userId(user.getId())
                    .code(code)
                    .type(TokenType.PASSWORD_RESET)
                    .expiresAt(Instant.now().plusSeconds(60 * 30)) // 30 minutes
                    .build();
            tokenRepository.save(token);

            // Use HTML template instead of plain text
            try {
                String html = EmailTemplates.passwordReset(user.getFullName(), code);
                emailService.sendHtmlMessage(email, "Réinitialisation de votre mot de passe", html);
            } catch (Exception ex) {
                // Log but don't throw - email failure shouldn't block password reset request
                System.err.println("Failed to send password reset email: " + ex.getMessage());
            }
        });
    }

    public void resetPassword(String code, String newPassword) {
        Optional<VerificationToken> ot = tokenRepository.findByCodeAndType(code, TokenType.PASSWORD_RESET);
        VerificationToken token = ot.orElseThrow(() -> new IllegalArgumentException("Invalid code"));
        if (token.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Code expired");
        }
        String userId = token.getUserId();
        userService.findById(userId).ifPresent(user -> {
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(Instant.now());
            userService.update(user.getId(), user);
        });
        tokenRepository.delete(token);
    }

    public User getLoggedInUser(String emailOrId) {
        if (emailOrId == null || emailOrId.isBlank()) {
            throw new IllegalArgumentException("User not found");
        }

        String key = emailOrId.trim();

        // 1) exact match
        var opt = userService.findByEmail(key);
        if (opt.isPresent()) return opt.get();

        // 2) lowercase match
        opt = userService.findByEmail(key.toLowerCase());
        if (opt.isPresent()) return opt.get();

        // 3) uppercase match (rare but harmless)
        opt = userService.findByEmail(key.toUpperCase());
        if (opt.isPresent()) return opt.get();

        // 4) fallback to id (subject from token)
        opt = userService.findById(key);
        if (opt.isPresent()) return opt.get();

        throw new IllegalArgumentException("User not found");
    }

    public java.util.List<java.util.Map<String,String>> listAllUsersShort() {
        var all = userService.findAll();
        java.util.List<java.util.Map<String,String>> out = new java.util.ArrayList<>();
        for (var u : all) {
            out.add(java.util.Map.of("id", u.getId(), "email", u.getEmail()));
        }
        return out;
    }

    private String generateCode() {
        int number = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(number);
    }
}
