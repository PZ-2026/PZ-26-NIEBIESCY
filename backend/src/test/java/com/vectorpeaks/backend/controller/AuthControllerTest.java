///*
// * AuthControllerTest.java
// *
// * Version: 1.1
// * Date: 2026-05-17
// *
// * Copyright (c) 2026 EduLink Team. All rights reserved.
// */
//
//package com.vectorpeaks.backend.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.vectorpeaks.backend.dto.LoginRequest;
//import com.vectorpeaks.backend.dto.RefreshRequest;
//import com.vectorpeaks.backend.entity.RefreshToken;
//import com.vectorpeaks.backend.entity.User;
//import com.vectorpeaks.backend.repository.UserRepository;
//import com.vectorpeaks.backend.security.JwtUtil;
//import com.vectorpeaks.backend.service.AuthService;
//import com.vectorpeaks.backend.service.FcmTokenService;
//import com.vectorpeaks.backend.service.LoginAttemptService;
//import com.vectorpeaks.backend.service.RefreshTokenService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.bean.override.mockito.MockitoBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//import java.util.Optional;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Unit tests for {@link AuthController}.
// *
// * Verifies login, token refresh, and logout endpoints.
// * Uses @WebMvcTest — only the web layer is loaded.
// * All dependencies are replaced by Mockito mocks.
// *
// * @version 1.1
// * @author EduLink Team
// */
//@WebMvcTest(
//        controllers = AuthController.class,
//        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
//)
//class AuthControllerTest extends BaseControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockitoBean
//    private AuthService authService;
//
//    @MockitoBean
//    private RefreshTokenService refreshTokenService;
//
//    @MockitoBean
//    private LoginAttemptService loginAttemptService;
//
//    @MockitoBean
//    private UserRepository userRepository;
//
//    @MockitoBean
//    private FcmTokenService fcmTokenService;
//
//    private User mockUser;
//    private RefreshToken mockRefreshToken;
//
//    @BeforeEach
//    void setUp() {
//        mockUser = new User();
//        mockUser.setId(1);
//        mockUser.setEmail("jan@example.com");
//        mockUser.setFirstName("Jan");
//        mockUser.setLastName("Kowalski");
//        mockUser.setRoleId(3); // STUDENT
//        mockUser.setAccountStatusId(1);
//
//        mockRefreshToken = new RefreshToken();
//        mockRefreshToken.setToken("valid-refresh-uuid");
//        mockRefreshToken.setUserId(1);
//        mockRefreshToken.setExpiresAt(Instant.now().plusSeconds(604800));
//        mockRefreshToken.setRevoked(false);
//    }
//
//    // -----------------------------------------------------------------------
//    // POST /api/auth/login
//    // -----------------------------------------------------------------------
//
//    @Nested
//    @DisplayName("POST /api/auth/login")
//    class LoginTests {
//
//        @Test
//        @DisplayName("Poprawne dane → 200 OK z tokenem i refreshToken")
//        void login_validCredentials_returns200WithTokens() throws Exception {
//            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
//            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
//            when(authService.authenticate("jan@example.com", "haslo123"))
//                    .thenReturn(Optional.of(mockUser));
//            when(jwtUtil.generateToken(anyInt(), anyString(), anyString()))
//                    .thenReturn("mock-jwt-token");
//            when(refreshTokenService.createRefreshToken(1))
//                    .thenReturn(mockRefreshToken);
//
//            LoginRequest request = new LoginRequest();
//            request.setEmail("jan@example.com");
//            request.setPassword("haslo123");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.token").value("mock-jwt-token"))
//                    .andExpect(jsonPath("$.refreshToken").value("valid-refresh-uuid"))
//                    .andExpect(jsonPath("$.email").value("jan@example.com"));
//        }
//
//        @Test
//        @DisplayName("Złe hasło → 401 Unauthorized")
//        void login_wrongPassword_returns401() throws Exception {
//            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
//            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
//            when(authService.authenticate(anyString(), anyString()))
//                    .thenReturn(Optional.empty());
//
//            LoginRequest request = new LoginRequest();
//            request.setEmail("jan@example.com");
//            request.setPassword("zle-haslo");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//        }
//
//        @Test
//        @DisplayName("Konto zablokowane (brute-force) → 429 Too Many Requests")
//        void login_blockedAccount_returns429() throws Exception {
//            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(true);
//
//            LoginRequest request = new LoginRequest();
//            request.setEmail("jan@example.com");
//            request.setPassword("haslo123");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isTooManyRequests());
//        }
//
//        @Test
//        @DisplayName("Pusty email → 401 (nie ujawniamy czy email istnieje)")
//        void login_emptyEmail_returns401() throws Exception {
//            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
//            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
//            when(authService.authenticate(anyString(), anyString()))
//                    .thenReturn(Optional.empty());
//
//            LoginRequest request = new LoginRequest();
//            request.setEmail("");
//            request.setPassword("haslo123");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//        }
//
//        @Test
//        @DisplayName("Odpowiedź błędu logowania nie ujawnia czy email istnieje")
//        void login_wrongCredentials_sameMessageRegardlessOfEmailExistence() throws Exception {
//            when(loginAttemptService.isBlocked(anyString(), anyString())).thenReturn(false);
//            when(loginAttemptService.getDelayMs(anyString(), anyString())).thenReturn(0L);
//            when(authService.authenticate(anyString(), anyString()))
//                    .thenReturn(Optional.empty());
//
//            LoginRequest request = new LoginRequest();
//            request.setEmail("nieistniejacy@example.com");
//            request.setPassword("jakies-haslo");
//
//            mockMvc.perform(post("/api/auth/login")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized())
//                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid email or password.")));
//        }
//    }
//
//    // -----------------------------------------------------------------------
//    // POST /api/auth/refresh
//    // -----------------------------------------------------------------------
//
//    @Nested
//    @DisplayName("POST /api/auth/refresh")
//    class RefreshTests {
//
//        @Test
//        @DisplayName("Ważny refresh token → 200 OK z nowym access tokenem")
//        void refresh_validToken_returns200WithNewAccessToken() throws Exception {
//            when(refreshTokenService.validateRefreshToken("valid-refresh-uuid"))
//                    .thenReturn(Optional.of(mockRefreshToken));
//            when(userRepository.findById(1))
//                    .thenReturn(Optional.of(mockUser));
//            when(jwtUtil.generateToken(anyInt(), anyString(), anyString()))
//                    .thenReturn("new-jwt-token");
//
//            RefreshRequest request = new RefreshRequest();
//            request.setRefreshToken("valid-refresh-uuid");
//
//            mockMvc.perform(post("/api/auth/refresh")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.token").value("new-jwt-token"));
//        }
//
//        @Test
//        @DisplayName("Wygasły lub unieważniony refresh token → 401")
//        void refresh_expiredToken_returns401() throws Exception {
//            when(refreshTokenService.validateRefreshToken("expired-token"))
//                    .thenReturn(Optional.empty());
//
//            RefreshRequest request = new RefreshRequest();
//            request.setRefreshToken("expired-token");
//
//            mockMvc.perform(post("/api/auth/refresh")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//        }
//
//        @Test
//        @DisplayName("Nieznany refresh token → 401")
//        void refresh_unknownToken_returns401() throws Exception {
//            when(refreshTokenService.validateRefreshToken("unknown-token"))
//                    .thenReturn(Optional.empty());
//
//            RefreshRequest request = new RefreshRequest();
//            request.setRefreshToken("unknown-token");
//
//            mockMvc.perform(post("/api/auth/refresh")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andExpect(status().isUnauthorized());
//        }
//    }
//
//    // -----------------------------------------------------------------------
//    // POST /api/auth/logout
//    // -----------------------------------------------------------------------
//
//    @Nested
//    @DisplayName("POST /api/auth/logout")
//    class LogoutTests {
//
//        @Test
//        @DisplayName("Logout z ważnym tokenem → 200 OK")
//        void logout_validToken_returns200() throws Exception {
//            mockMvc.perform(post("/api/auth/logout")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("{\"refreshToken\":\"valid-refresh-uuid\",\"userId\":1,\"fcmToken\":\"fcm-abc\"}"))
//                    .andExpect(status().isOk());
//        }
//
//        @Test
//        @DisplayName("Logout z nieistniejącym tokenem → też 200 (idempotentny)")
//        void logout_unknownToken_returns200() throws Exception {
//            // revokeToken() na nieistniejącym tokenie nie rzuca wyjątku
//            mockMvc.perform(post("/api/auth/logout")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content("{\"refreshToken\":\"ghost-token\",\"userId\":99}"))
//                    .andExpect(status().isOk());
//        }
//    }
//}