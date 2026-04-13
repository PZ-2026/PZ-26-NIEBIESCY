/*
 * AuthController.java
 *
 * Version: 1.0
 * Date: 2026-04-13
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Handles authentication-related HTTP requests.
 * Provides endpoints for user login.
 *
 * @version 1.0
 * @author EduLink Team
 */

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * Authenticates a user based on email and password.
     *
     * @param request the login request containing email and password
     * @return ResponseEntity containing user data if authentication succeeds,
     *         or UNAUTHORIZED status with error message otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Attempt to authenticate using the auth service
        Optional<User> userOpt = authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LoginResponse response = new LoginResponse();
            response.setId(user.getId());
            response.setFirstName(user.getFirstName());
            response.setLastName(user.getLastName());
            response.setEmail(user.getEmail());
            response.setRole(String.valueOf(user.getRoleId()));
            return ResponseEntity.ok(response);
        } else {
            // Return 401 Unauthorized with a plain text error message
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Nieprawidłowy email lub hasło");
        }
    }

}

