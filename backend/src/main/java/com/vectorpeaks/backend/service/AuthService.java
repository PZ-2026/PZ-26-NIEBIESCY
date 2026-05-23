/*
 * AuthService.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service class responsible for authentication logic.
 * Handles user verification and password hashing.
 *
 * @version 1.0
 * @author EduLink Team
 */

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * Authenticates a user by email and plain-text password.
     * Compares the provided password with the stored BCrypt hash.
     *
     * @param email       the user's email address
     * @param rawPassword the plain-text password to verify
     * @return an {@link Optional} containing the authenticated user if
     *         credentials are valid; empty otherwise
     */
    public Optional<User> authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
            if (matches) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
