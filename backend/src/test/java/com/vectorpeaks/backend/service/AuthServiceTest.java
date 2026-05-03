/*
 * AuthServiceTest.java
 *
 * Version: 1.2
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
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
 *   <li>successful login with valid credentials,</li>
 *   <li>rejection on incorrect e-mail or password,</li>
 *   <li>handling of empty input values,</li>
 *   <li>correct interaction with {@link UserRepository}.</li>
 * </ul>
 *
 * <p>Uses Mockito ({@code @ExtendWith(MockitoExtension.class)}) –
 * no Spring context or database is started.
 *
 * @version 1.2
 * @author EduLink Team
 * @see AuthService
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    /** Mock of the user repository – replaces the database layer. */
    @Mock
    private UserRepository userRepository;

    /**
     * The object under test with the {@link UserRepository} mock
     * automatically injected.
     */
    @InjectMocks
    private AuthService authService;

    /** Helper encoder used to create hashed passwords in tests. */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** Sample user initialised before each test. */
    private User mockUser;

    /**
     * Initialises a sample user with a hashed password ({@code "tajneHaslo123"})
     * before each test case.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("jan.kowalski@example.com");
        mockUser.setFirstName("Jan");
        mockUser.setLastName("Kowalski");
        mockUser.setRoleId(1);
        mockUser.setPassword(encoder.encode("tajneHaslo123"));
    }

    // -----------------------------------------------------------------------
    // authenticate() – success cases
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@code authenticate} returns the user wrapped in an
     * {@link Optional} when the provided e-mail and password are correct.
     */
    @Test
    void authenticate_validCredentials_returnsUser() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "tajneHaslo123");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("jan.kowalski@example.com");
    }

    // -----------------------------------------------------------------------
    // authenticate() – error cases
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@code authenticate} returns an empty {@link Optional}
     * when the provided e-mail does not exist in the database.
     */
    @Test
    void authenticate_unknownEmail_returnsEmpty() {
        when(userRepository.findByEmail("missing@example.com"))
                .thenReturn(Optional.empty());

        Optional<User> result = authService.authenticate(
                "missing@example.com", "tajneHaslo123");

        assertThat(result).isEmpty();
    }

    /**
     * Verifies that {@code authenticate} returns an empty {@link Optional}
     * when the e-mail exists but the provided password is incorrect.
     */
    @Test
    void authenticate_wrongPassword_returnsEmpty() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "wrongPassword");

        assertThat(result).isEmpty();
    }

    /**
     * Verifies that {@code authenticate} returns an empty {@link Optional}
     * when the provided e-mail is an empty string.
     */
    @Test
    void authenticate_emptyEmail_returnsEmpty() {
        when(userRepository.findByEmail(""))
                .thenReturn(Optional.empty());

        Optional<User> result = authService.authenticate("", "tajneHaslo123");

        assertThat(result).isEmpty();
    }

    /**
     * Verifies that {@code authenticate} returns an empty {@link Optional}
     * when the provided password is an empty string.
     */
    @Test
    void authenticate_emptyPassword_returnsEmpty() {
        when(userRepository.findByEmail("jan.kowalski@example.com"))
                .thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.authenticate(
                "jan.kowalski@example.com", "");

        assertThat(result).isEmpty();
    }

    // -----------------------------------------------------------------------
    // Repository interaction verification
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@code authenticate} calls
     * {@link UserRepository#findByEmail(String)} exactly once with the
     * provided e-mail address and performs no other operations on the repository.
     */
    @Test
    void authenticate_alwaysSearchesByEmail() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        authService.authenticate("someone@example.com", "password");

        verify(userRepository, times(1)).findByEmail("someone@example.com");
        verifyNoMoreInteractions(userRepository);
    }
}