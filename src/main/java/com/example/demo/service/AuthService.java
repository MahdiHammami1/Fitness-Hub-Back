package com.example.demo.service;

import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.model.VerificationToken;
import com.example.demo.model.TokenType;
import com.example.demo.repository.VerificationTokenRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService,
                       VerificationTokenRepository tokenRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public String signUp(String email, String password, String fullName) {
        if (userService.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already used");
        }
        User u = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(Role.CUSTOMER)
                .enabled(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        User created = userService.create(u);

        String code = generateCode();
        VerificationToken token = VerificationToken.builder()
                .userId(created.getId())
                .code(code)
                .type(com.example.demo.model.TokenType.VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(60L * 60 * 24))
                .build();
        tokenRepository.save(token);

        String body = "Votre code de vérification: " + code;
        emailService.sendSimpleMessage(email, "Vérification de votre compte", body);

        // return JWT even if user is not yet enabled; token subject is the email
        return jwtUtil.generateToken(created.getEmail());
    }

    public String signIn(String email, String password) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );
        // Si l'auth succeed, générer token
        return jwtUtil.generateToken(email);
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
            String body = "Votre code de réinitialisation : " + code;
            emailService.sendSimpleMessage(email, "Réinitialisation de mot de passe", body);
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

    public User getLoggedInUser(String email) {
        return userService.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private String generateCode() {
        Random rnd = new Random();
        int number = 100000 + rnd.nextInt(900000);
        return String.valueOf(number);
    }
}
