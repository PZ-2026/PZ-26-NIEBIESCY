package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.RefreshToken;
import com.vectorpeaks.backend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages creation, validation and revocation of refresh tokens.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Service
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Creates a refresh token for the user.
     * Revokes any existing tokens first — one active token per user (single-device).
     *
     * For multi-device support: remove revokeAllByUserId() call.
     */
    public RefreshToken createRefreshToken(Integer userId) {
        // Invalidate this user's previous tokens
        refreshTokenRepository.revokeAllByUserId(userId);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Finds and validates a refresh token string.
     * Returns empty if not found, revoked, or expired.
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isRevoked())
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()));
    }

    /**
     * Revokes a single refresh token (logout from single device).
     */
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        });
    }

    /**
     * Revokes all tokens for a user (logout from all devices).
     */
    public void revokeAllTokensForUser(Integer userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}