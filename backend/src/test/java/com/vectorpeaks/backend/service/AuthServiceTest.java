/*
 * AuthServiceTest.java
 *
 * Version: 1.3
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 *
 * <p>Verifies the user authentication logic, including:
 * <ul>
 * <li>successful login with valid credentials and active status,</li>
 * <li>failed login due to invalid passwords or nonexistent emails,</li>
 * <li>failed login due to blocked, suspended, or missing account statuses.</li>
 * </ul>
 *
 * @version 1.3
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private final String RAW_PASSWORD = "SecurePassword123!";
    private String encodedPassword;

    /**
     * Prepares standard infrastructure dependencies and initial entity states
     * prior to the execution of individual test cases.
     */
    @BeforeEach
    void setUp() {
        // We use a real encoder to match the internal implementation of AuthService
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        encodedPassword = encoder.encode(RAW_PASSWORD);

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("test@example.com");
        testUser.setPassword(encodedPassword);
        testUser.setAccountStatusId(1); // 1 = ACTIVE by default
    }

    /**
     * Verifies that authenticating with complete and accurate parameters
     * for an active account successfully returns the user object.
     */
    @Test
    @DisplayName("Valid credentials & active account → returns User")
    void authenticate_validCredentialsAndActiveAccount_returnsUser() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.authenticate(testUser.getEmail(), RAW_PASSWORD);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    /**
     * Confirms that login configurations reject authentication
     * whenever password parameters mismatch expected storage patterns.
     */
    @Test
    @DisplayName("Wrong password → returns Optional.empty()")
    void authenticate_wrongPassword_returnsEmpty() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.authenticate(testUser.getEmail(), "WrongPassword999!");

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Confirms that attempting to authenticate with an email not present
     * in the system returns an empty result.
     */
    @Test
    @DisplayName("Non-existent user → returns Optional.empty()")
    void authenticate_userNotFound_returnsEmpty() {
        // Arrange
        when(userRepository.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = authService.authenticate("ghost@example.com", RAW_PASSWORD);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Asserts authorization processing blocks continuation whenever
     * the account status is marked as blocked (e.g., status != 1),
     * even if the credentials are perfectly valid.
     */
    @Test
    @DisplayName("Valid credentials but BLOCKED account (status 2) → returns Optional.empty()")
    void authenticate_validCredentialsButBlockedAccount_returnsEmpty() {
        // Arrange
        testUser.setAccountStatusId(2); // 2 = BLOCKED
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.authenticate(testUser.getEmail(), RAW_PASSWORD);

        // Assert
        assertThat(result).isEmpty();
    }

    /**
     * Ensures that corrupted or uninitialized account statuses (null)
     * default to a secure, locked state rejecting authentication.
     */
    @Test
    @DisplayName("Valid credentials but NULL account status → returns Optional.empty()")
    void authenticate_validCredentialsButNullStatus_returnsEmpty() {
        // Arrange
        testUser.setAccountStatusId(null);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = authService.authenticate(testUser.getEmail(), RAW_PASSWORD);

        // Assert
        assertThat(result).isEmpty();
    }
}