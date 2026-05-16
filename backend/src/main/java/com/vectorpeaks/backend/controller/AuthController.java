/*
 * AuthController.java
 *
 * Version: 1.1
 * Date: 2026-05-15
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.dto.LoginResponse;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.security.JwtUtil;
import com.vectorpeaks.backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Handles authentication-related HTTP requests.
 * Provides endpoints for user login and JWT token issuance.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * Constructs a new AuthController with the required security services.
     *
     * @param authService the authentication service for credential verification
     * @param jwtUtil     the utility service for JWT token generation
     */
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user and returns a JWT access token.
     *
     * <p>Checks credentials via {@link AuthService} and, if valid,
     * generates a signed JWT token containing user identity.
     *
     * @param request the login request containing email and password
     * @return ResponseEntity containing user profile and JWT token if successful,
     *         or UNAUTHORIZED status if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = authService.authenticate(request.getEmail(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            String roleName = user.getRoleName();

            // Generate the access token for the authenticated user
            String token = jwtUtil.generateToken(user.getId(), user.getEmail(), roleName);

            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setRole(String.valueOf(user.getRoleId()));
            response.setToken(token);

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Nieprawidłowy e-mail lub hasło.");
        }
    }
}