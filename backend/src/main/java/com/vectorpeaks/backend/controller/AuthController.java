/*
 * AuthController.java
 *
 * Version: 1.2
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
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
import com.vectorpeaks.backend.service.MaintenanceService;
import com.vectorpeaks.backend.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST controller handling authentication-related HTTP requests.
 * * <p>Exposes the following endpoints:
 * <ul>
 * <li>{@code POST /api/auth/login} – credentials verification, returns access and refresh tokens</li>
 * <li>{@code POST /api/auth/refresh} – exchanges a refresh token for a new access token</li>
 * <li>{@code POST /api/auth/logout} – invalidates the refresh token in the database</li>
 * <li>{@code POST /api/auth/users/{userId}/fcm-token} – registers the device's FCM token</li>
 * </ul>
 *
 * @version 1.2
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final LoginAttemptService loginAttemptService;
    private final UserRepository userRepository;
    private final FcmTokenService fcmTokenService;
    private final MaintenanceService maintenanceService;

    public AuthController(AuthService authService,
                          JwtUtil jwtUtil,
                          RefreshTokenService refreshTokenService,
                          LoginAttemptService loginAttemptService,
                          UserRepository userRepository,
                          FcmTokenService fcmTokenService,
                          MaintenanceService maintenanceService) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
        this.loginAttemptService = loginAttemptService;
        this.userRepository = userRepository;
        this.fcmTokenService = fcmTokenService;
        this.maintenanceService = maintenanceService;
    }

    /**
     * Authenticates the user and returns access and refresh tokens.
     * Includes brute-force protection and progressive delay.
     *
     * @param request credentials request
     * @param httpRequest HTTP request context for IP resolution
     * @return {@code 200 OK} with tokens, or error status
     */
    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String ip = httpRequest.getRemoteAddr();

        if (loginAttemptService.isBlocked(email, ip)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many login attempts. Please try again in 15 minutes.");
        }

        long delayMs = loginAttemptService.getDelayMs(email, ip);
        if (delayMs > 0) {
            try { Thread.sleep(delayMs); } catch (InterruptedException ignored) {}
        }

        Optional<User> userOpt = authService.authenticate(email, request.getPassword());
        if (userOpt.isEmpty()) {
            loginAttemptService.recordFailure(email, ip);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password.");
        }

        User user = userOpt.get();

        if (maintenanceService.isFullyActive() && !"ADMIN".equalsIgnoreCase(user.getRoleName())) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Trwają prace serwisowe");
        }

        if (user.getAccountStatusId() == null || user.getAccountStatusId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Twoje konto jest zablokowane");
        }

        loginAttemptService.recordSuccess(email, ip);

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRoleName());
        RefreshToken refresh = refreshTokenService.createRefreshToken(user.getId());

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(String.valueOf(user.getRoleId()));
        response.setToken(accessToken);
        response.setRefreshToken(refresh.getToken());

        return ResponseEntity.ok(response);
    }

    /**
     * Exchanges a valid refresh token for a new access token.
     *
     * @param request refresh token request
     * @return {@code 200 OK} with the new access token, or {@code 401 Unauthorized}
     */
    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest request) {
        return refreshTokenService.validateRefreshToken(request.getRefreshToken())
                .map(rt -> {
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
     * Logs out the user by revoking the refresh token and removing the FCM token.
     *
     * @param request logout request containing tokens
     * @return {@code 200 OK} upon successful logout
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
        refreshTokenService.revokeToken(request.getRefreshToken());
        if (request.getFcmToken() != null) {
            fcmTokenService.removeToken(request.getFcmToken());
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully."));
    }

}