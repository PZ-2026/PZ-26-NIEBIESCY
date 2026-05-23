/*
 * JwtUtil.java
 *
 * Version: 1.0
 * Date: 2026-05-14
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utility class for JWT token generation, validation, and parsing.
 *
 * <p>Handles:
 * <ul>
 *   <li>generating access tokens with user ID, email, and role,</li>
 *   <li>validating token signature and expiration,</li>
 *   <li>extracting user information from valid tokens.</li>
 * </ul>
 *
 * @version 1.0
 * @author EduLink Team
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generates a new access token from a User entity.
     * Used by the /refresh endpoint after validating the refresh token.
     *
     * @param userId the user's ID
     * @param email  the user's email
     * @param role   the user's role name
     * @return signed JWT access token
     */
    public String generateToken(Integer userId, String email, String role) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Validates the JWT token signature and expiration.
     *
     * @param token the JWT token string
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Extracts the user ID (subject) from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the user ID as Integer, or null if token is invalid
     */
    public Integer getUserIdFromToken(String token) {
        try {
            String subject = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return Integer.parseInt(subject);
        } catch (JwtException | NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Extracts the email claim from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the email claim, or null if token is invalid
     */
    public String getEmailFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("email", String.class);
        } catch (JwtException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Extracts the role claim from a valid JWT token.
     *
     * @param token the JWT token string
     * @return the role claim, or null if token is invalid
     */
    public String getRoleFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("role", String.class);
        } catch (JwtException | NullPointerException e) {
            return null;
        }
    }
}
