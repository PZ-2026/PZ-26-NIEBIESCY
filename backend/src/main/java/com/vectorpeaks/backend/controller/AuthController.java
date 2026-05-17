/*
 * AuthController.java
 *
 * Version: 1.1
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.dto.LoginResponse;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Handles authentication-related HTTP requests.
 * Provides endpoints for user login.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    /**
     * Constructs a new AuthController with the given AuthService.
     *
     * @param authService the authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Authenticates a user based on email and password.
     *
     * @param request the login request containing email and password
     * @return ResponseEntity containing user data if authentication succeeds,
     *         or UNAUTHORIZED status with error message otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String email = request.getEmail();
        logger.info("Login attempt for email: {}", email);

        Optional<User> userOpt = authService.authenticate(
                email,
                request.getPassword()
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            logger.info("Login successful for email: {}, userId: {}, roleId: {}",
                    email, user.getId(), user.getRoleId());

            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setRole(String.valueOf(user.getRoleId()));
            return ResponseEntity.ok(response);
        } else {
            logger.error("Login failed – invalid credentials for email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Nieprawidłowy email lub hasło");
        }
    }
}