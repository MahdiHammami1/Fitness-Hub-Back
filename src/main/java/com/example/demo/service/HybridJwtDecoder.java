package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hybrid JWT Decoder that supports both:
 * 1. Local HS256 tokens (signed with jwt.secret)
 * 2. Remote RS256 tokens from Keycloak (or other OAuth2 providers)
 *
 * Strategy:
 * - Extract algorithm from JWT header
 * - If HS*, use local secret to verify
 * - If RS*, extract issuer from payload and create/cache RS decoder dynamically
 */
@Component
@Slf4j
public class HybridJwtDecoder implements JwtDecoder {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, JwtDecoder> rsDecoderCache = new ConcurrentHashMap<>();

    @Override
    public Jwt decode(String token) throws BadJwtException {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BadJwtException("Token format invalid: expected 3 parts");
            }

            // Decode header to check algorithm
            String headerJson = decodeBase64Url(parts[0]);
            JsonNode header = objectMapper.readTree(headerJson);
            String alg = header.has("alg") ? header.get("alg").asText("") : "";

            log.debug("Token algorithm: {}", alg);

            if (alg.startsWith("HS")) {
                // Local token - verify with HS256
                return decodeHSToken(token);
            } else if (alg.startsWith("RS")) {
                // Remote token - extract issuer and get appropriate RS decoder
                return decodeRSToken(token, parts[1]);
            } else {
                throw new BadJwtException("Unsupported algorithm: " + alg);
            }

        } catch (BadJwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error decoding token: {}", e.getMessage());
            throw new BadJwtException("Failed to decode token: " + e.getMessage(), e);
        }
    }

    /**
     * Decode and verify HS* (HMAC) token using local secret
     */
    private Jwt decodeHSToken(String token) throws Exception {
        try {
            String[] parts = token.split("\\.");
            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            // Verify signature
            String expectedSignature = generateHSSignature(header + "." + payload);
            if (!signature.equals(expectedSignature)) {
                throw new BadJwtException("Invalid signature for HS token");
            }

            // Decode payload
            String payloadJson = decodeBase64Url(payload);
            JsonNode payloadNode = objectMapper.readTree(payloadJson);

            log.debug("HS token decoded successfully");

            // Convert JsonNode to Map for Jwt claims
            Map<String, Object> claims = objectMapper.convertValue(payloadNode, Map.class);

            // Reconstruct Jwt object (Spring OAuth2 Jwt class)
            return new Jwt(
                    token,
                    null, // issuedAt - not critical
                    null, // expiresAt - not critical
                    Map.of("alg", "HS256"),
                    claims
            );

        } catch (BadJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new BadJwtException("Invalid HS token: " + e.getMessage(), e);
        }
    }

    /**
     * Decode RS* (RSA) token using issuer from token
     */
    private Jwt decodeRSToken(String token, String payloadPart) throws Exception {
        try {
            String payloadJson = decodeBase64Url(payloadPart);
            JsonNode payload = objectMapper.readTree(payloadJson);

            if (!payload.has("iss")) {
                throw new BadJwtException("RS token missing 'iss' claim");
            }

            String issuer = payload.get("iss").asText();
            log.debug("Token issuer: {}", issuer);

            // Get or create RS decoder for this issuer
            JwtDecoder rsDecoder = rsDecoderCache.computeIfAbsent(issuer, iss -> {
                try {
                    log.info("Creating JwtDecoder for issuer: {}", iss);
                    return JwtDecoders.fromIssuerLocation(iss);
                } catch (Exception e) {
                    log.error("Failed to create JwtDecoder for issuer {}: {}", iss, e.getMessage());
                    throw new RuntimeException("Cannot create RS decoder for issuer: " + iss, e);
                }
            });

            return rsDecoder.decode(token);

        } catch (BadJwtException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error decoding RS token: {}", e.getMessage());
            throw new BadJwtException("Invalid RS token: " + e.getMessage(), e);
        }
    }

    /**
     * Generate HS256 signature using jwt.secret
     */
    private String generateHSSignature(String data) throws Exception {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "HmacSHA256");

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKey);
        byte[] signatureBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }

    /**
     * Decode Base64 URL-encoded string
     */
    private String decodeBase64Url(String encoded) {
        return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}

