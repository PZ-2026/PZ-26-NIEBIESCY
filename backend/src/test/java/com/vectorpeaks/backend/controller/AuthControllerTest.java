/*
 * AuthControllerTest.java
 *
 * Version: 1.1
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.dto.RefreshRequest;
import com.vectorpeaks.backend.entity.RefreshToken;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import com.vectorpeaks.backend.security.JwtUtil;
import com.vectorpeaks.backend.service.AuthService;
import com.vectorpeaks.backend.service.FcmTokenService;
import com.vectorpeaks.backend.service.LoginAttemptService;
import com.vectorpeaks.backend.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link AuthController}.
 *
 * <p>Verifies endpoint security rules and response payloads for:
 * <ul>
 *   <li>User login and authentication tracking,</li>
 *   <li>JWT access token refreshing,</li>
 *   <li>User logout and token revocation.</li>
 * </ul>
 *
 * Uses {@code @WebMvcTest} where only the web layer is loaded.
 * All application services and data repositories are fully mocked.
 *
 * @version 1.1
 * @author EduLink Team
 */
@WebMvcTest(
        controllers = AuthController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class AuthControllerTest extends BaseControllerTest {

    /**
     * Main entry point for server-side Spring MVC test support.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper instance utilized to map data structures to JSON formats.
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Mocked authentication processing service.
     */
    @MockitoBean
    private AuthService authService;

    /**
     * Mocked database token lifespan and validation manager.
     */
    @MockitoBean
    private RefreshTokenService refreshTokenService;

    /**
     * Mocked brute-force prevention and rate-limiting system tracker.
     */
    @MockitoBean
    private LoginAttemptService loginAttemptService;

    /**
     * Mocked core repository providing application user account access.
     */
    @MockitoBean
    private UserRepository userRepository;

    /**
     * Mocked service managing registration identifiers for Firebase Cloud Messaging.
     */
    @MockitoBean
    private FcmTokenService fcmTokenService;

    /**
     * Reusable mock user data record initialized before individual test setups.
     */
    private User mockUser;

    /**
     * Reusable mock refresh token instance verifying session persistence flows.
     */
    private RefreshToken mockRefreshToken;

    /**
     * Prepares standard infrastructure dependencies and initial entity states
     * prior to the execution of individual test cases.
     */
    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setEmail("jan@example.com");
        mockUser.setFirstName("Jan");
        mockUser.setLastName("Kowalski");
        mockUser.setRoleId(3); // STUDENT
        mockUser.setAccountStatusId(1);

        mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("valid-refresh-uuid");
        mockRefreshToken.setUserId(1);
        mockRefreshToken.setExpiresAt(Instant.now().plusSeconds(604800));
        mockRefreshToken.setRevoked(false);
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/login
    // -----------------------------------------------------------------------

    /**
     * Comprehensive web context validation scenarios targeted directly at
     * user login attempts.
     */
    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginTests {

        /**
         * Verifies that authenticating with complete and accurate parameters
         * successfully produces an active access payload matching client properties.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Poprawne dane → 200 OK z tokenem i refreshToken")
        void login_validCredentials_returns200WithTokens() throws Exception {
            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
            when(authService.authenticate("jan@example.com", "haslo123"))
                    .thenReturn(Optional.of(mockUser));
            when(jwtUtil.generateToken(anyInt(), anyString(), anyString()))
                    .thenReturn("mock-jwt-token");
            when(refreshTokenService.createRefreshToken(1))
                    .thenReturn(mockRefreshToken);

            LoginRequest request = new LoginRequest();
            request.setEmail("jan@example.com");
            request.setPassword("haslo123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mock-jwt-token"))
                    .andExpect(jsonPath("$.refreshToken").value("valid-refresh-uuid"))
                    .andExpect(jsonPath("$.email").value("jan@example.com"));
        }

        /**
         * Confirms that login configurations supply standard authentication rejects
         * whenever password parameters mismatch expected storage patterns.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Złe hasło → 401 Unauthorized")
        void login_wrongPassword_returns401() throws Exception {
            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
            when(authService.authenticate(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest();
            request.setEmail("jan@example.com");
            request.setPassword("zle-haslo");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * Confirms security safeguards block inbound requests entirely when repetitive
         * incorrect credentials exceed tracking bounds.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Konto zablokowane (brute-force) → 429 Too Many Requests")
        void login_blockedAccount_returns429() throws Exception {
            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(true);

            LoginRequest request = new LoginRequest();
            request.setEmail("jan@example.com");
            request.setPassword("haslo123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isTooManyRequests());
        }

        /**
         * Validates security guidelines ensuring incomplete string components return
         * identical generic rejections to prevent database username enumeration.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Pusty email → 401 (nie ujawniamy czy email istnieje)")
        void login_emptyEmail_returns401() throws Exception {
            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
            when(authService.authenticate(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest();
            request.setEmail("");
            request.setPassword("haslo123");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * Confirms response payload feedback messages remain uniform regardless of
         * user email structure availability in underlying persistence schemas.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Odpowiedź błędu logowania nie ujawnia czy email istnieje")
        void login_wrongCredentials_sameMessageRegardlessOfEmailExistence() throws Exception {
            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
            when(authService.authenticate(anyString(), anyString()))
                    .thenReturn(Optional.empty());

            LoginRequest request = new LoginRequest();
            request.setEmail("nieistniejacy@example.com");
            request.setPassword("jakies-haslo");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid email or password.")));
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/refresh
    // -----------------------------------------------------------------------

    /**
     * Web layer scenario suite verifying Token validation and regeneration.
     */
    @Nested
    @DisplayName("POST /api/auth/refresh")
    class RefreshTests {

        /**
         * Ensures a fully structural refresh workflow supplies authentic replacement
         * short-lived tokens upon request.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Ważny refresh token → 200 OK z nowym access tokenem")
        void refresh_validToken_returns200WithNewAccessToken() throws Exception {
            when(refreshTokenService.validateRefreshToken("valid-refresh-uuid"))
                    .thenReturn(Optional.of(mockRefreshToken));
            when(userRepository.findById(1))
                    .thenReturn(Optional.of(mockUser));
            when(jwtUtil.generateToken(anyInt(), anyString(), anyString()))
                    .thenReturn("new-jwt-token");

            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("valid-refresh-uuid");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("new-jwt-token"));
        }

        /**
         * Asserts authorization processing blocks continuation whenever lifetime bounds
         * on parameters are completely exceeded.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Wygasły lub unieważniony refresh token → 401")
        void refresh_expiredToken_returns401() throws Exception {
            when(refreshTokenService.validateRefreshToken("expired-token"))
                    .thenReturn(Optional.empty());

            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("expired-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        /**
         * Asserts token processor returns standard security denials when incoming strings
         * do not map into existing context engines.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Nieznany refresh token → 401")
        void refresh_unknownToken_returns401() throws Exception {
            when(refreshTokenService.validateRefreshToken("unknown-token"))
                    .thenReturn(Optional.empty());

            RefreshRequest request = new RefreshRequest();
            request.setRefreshToken("unknown-token");

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -----------------------------------------------------------------------
    // POST /api/auth/logout
    // -----------------------------------------------------------------------

    /**
     * Web verification assertions handling active token revocation processing.
     */
    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        /**
         * Validates standard user registration identity parameters discard context trackers
         * successfully upon requesting clean system exit states.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Logout z ważnym tokenem → 200 OK")
        void logout_validToken_returns200() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"valid-refresh-uuid\",\"userId\":1,\"fcmToken\":\"fcm-abc\"}"))
                    .andExpect(status().isOk());
        }

        /**
         * Asserts system exit routines execute smoothly without errors when receiving
         * requests for entries already dropped from storage states.
         *
         * @throws Exception if serialization or interface querying exceptions occur
         */
        @Test
        @DisplayName("Logout z nieistniejącym tokenem → też 200 (idempotentny)")
        void logout_unknownToken_returns200() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"refreshToken\":\"ghost-token\",\"userId\":99}"))
                    .andExpect(status().isOk());
        }
    }
}