/*
 * AuthController.java
 *
 * Version: 1.3
 * Date: 2026-05-16
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.LoginRequest;
import com.vectorpeaks.backend.dto.LoginResponse;
import com.vectorpeaks.backend.dto.LogoutRequest;
import com.vectorpeaks.backend.dto.RefreshRequest;
import com.vectorpeaks.backend.entity.RefreshToken;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import com.vectorpeaks.backend.security.JwtUtil;
import com.vectorpeaks.backend.service.AuthService;
import com.vectorpeaks.backend.service.FcmTokenService;
import com.vectorpeaks.backend.service.LoginAttemptService;
import com.vectorpeaks.backend.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Handles authentication: login, token refresh, and logout.
 *
 * Endpoints:
 *   POST /api/auth/login   – credentials verification, returns access + refresh token
 *   POST /api/auth/refresh – exchanges a refresh token for a new access token
 *   POST /api/auth/logout  – invalidates the refresh token in the database
 *
 * @version 1.3
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService         authService;
    private final JwtUtil             jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository      userRepository;
    private final FcmTokenService fcmTokenService;

    // Spring injects all dependencies via the constructor
    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService,
                          UserRepository userRepository,
                          FcmTokenService fcmTokenService) {
        this.authService         = authService;
        this.jwtUtil             = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.userRepository      = userRepository;
        this.fcmTokenService = fcmTokenService;
    }

    /**
     * POST /api/auth/login
     *
     * 1. Checks brute-force lockout status (email + IP)
     * 2. Verifies credentials
     * 3. Creates an access token (15 min) + a refresh token (7 days)
     * 4. Returns user data along with both tokens
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String ip    = httpRequest.getRemoteAddr();

        // Layer 1: brute-force check
        if (loginAttemptService.isBlocked(email, ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many login attempts. Please try again in 15 minutes.");
        }

        // Layer 2: progressive delay (3rd attempt = 2s, 4th attempt = 5s)
        long delayMs = loginAttemptService.getDelayMs(email, ip);
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
        }

        // Password verification
        Optional<User> userOpt = authService.authenticate(email, request.getPassword());

        if (userOpt.isEmpty()) {
            loginAttemptService.recordFailure(email, ip);
            // Intentionally identical message to mitigate username enumeration attacks
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        User user = userOpt.get();
        loginAttemptService.recordSuccess(email, ip);

        // Generate access token (JWT, 15 min)
        String accessToken = jwtUtil.generateToken(
                user.getId(), user.getEmail(), user.getRoleName());

        // Generate refresh token (UUID, 7 days, persisted in the database)
        RefreshToken refresh = refreshTokenService.createRefreshToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(String.valueOf(user.getRoleId()));
        response.setToken(accessToken);
        response.setRefreshToken(refresh.getToken()); // UUID string

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/refresh
     * Body: { "refreshToken": "uuid-string" }
     *
     * When the access token expires, the client application sends the refresh token here.
     * If valid → returns a new access token (without re-authentication).
     * If expired or revoked → returns 401, triggering the client app to redirect to the login screen.
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        return refreshTokenService.validateRefreshToken(request.getRefreshToken())
                .map(rt -> {
                    // Fetch fresh user data (e.g., user role might have changed in the meantime)
                    User user = userRepository.findById(rt.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    String newAccessToken = jwtUtil.generateToken(
                            user.getId(),
                            user.getEmail(),
                            user.getRoleName()
                    );

                    return ResponseEntity.ok(Map.of("token", newAccessToken));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Refresh token has expired. Please log in again.")));
    }

    /**
     * POST /api/auth/logout
     * Revokes the refresh token and removes the FCM token for the specific device.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());

        // Remove only this device's token — other devices remain active
        if (request.getFcmToken() != null) {
            fcmTokenService.removeToken(request.getFcmToken());
        }

        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

    /**
     * POST /api/users/{userId}/fcm-token
     * Registers the device's FCM token after a successful login.
     * Replaces the old updateFcmToken() call.
     */
    @PostMapping("/users/{userId}/fcm-token")
    public ResponseEntity<?> registerFcmToken(@PathVariable Integer userId,
                                              @RequestBody Map<String, String> body) {
        String fcmToken = body.get("fcmToken");
        fcmTokenService.registerToken(userId, fcmToken);
        return ResponseEntity.ok(Map.of("message", "FCM token registered successfully."));
    }
}