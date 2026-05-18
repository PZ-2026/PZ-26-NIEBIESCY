/*
 * RefreshTokenServiceTest.java
 *
 * Version: 1.0
 * Date: 2026-05-18
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.entity.RefreshToken;
import com.vectorpeaks.backend.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RefreshTokenService}.
 *
 * <p>Verifies token creation, validation (expired/revoked),
 * and revocation logic.
 *
 * @version 1.0
 * @author EduLink Team
 * @see RefreshTokenService
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    /** Mock of the refresh token database access layer. */
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    /** The instance under test with injected mocks. */
    @InjectMocks
    private RefreshTokenService refreshTokenService;

    /** Reusable valid refresh token entity stub configuration. */
    private RefreshToken validToken;

    /**
     * Prepares test fixtures and injects internal configuration fields via reflection before each test.
     */
    @BeforeEach
    void setUp() {
        // Manually set the field that normally comes from @Value
        org.springframework.test.util.ReflectionTestUtils.setField(
                refreshTokenService, "refreshExpirationMs", 604_800_000L);

        validToken = new RefreshToken();
        validToken.setToken("valid-uuid-token");
        validToken.setUserId(1);
        validToken.setExpiresAt(Instant.now().plusSeconds(3600));
        validToken.setRevoked(false);
    }

    // ── Token Creation ───────────────────────────────────────────

    /**
     * Verifies that creating a refresh token triggers the automatic revocation of previous
     * user tokens, persists the new entity entry, and returns it with appropriate attributes.
     */
    @Test
    @DisplayName("createRefreshToken saves the token to the database and returns it")
    void createRefreshToken_savesAndReturns() {
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        RefreshToken result = refreshTokenService.createRefreshToken(1);

        assertThat(result.getUserId()).isEqualTo(1);
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresAt()).isAfter(Instant.now());
        verify(refreshTokenRepository).revokeAllByUserId(1); // old tokens revoked
        verify(refreshTokenRepository).save(any());
    }

    // ── Token Validation ─────────────────────────────────────────

    /**
     * Verifies that a structurally valid and active token is accepted by the validation routine.
     */
    @Test
    @DisplayName("Valid token is accepted")
    void validateRefreshToken_validToken_returnsPresent() {
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isPresent();
    }

    /**
     * Verifies that a token flagged as revoked inside the database layer is rejected.
     */
    @Test
    @DisplayName("Revoked token is rejected")
    void validateRefreshToken_revokedToken_returnsEmpty() {
        validToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isEmpty();
    }

    /**
     * Verifies that a token whose expiration timestamp is in the past is rejected.
     */
    @Test
    @DisplayName("Expired token is rejected")
    void validateRefreshToken_expiredToken_returnsEmpty() {
        validToken.setExpiresAt(Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isEmpty();
    }

    /**
     * Verifies that searching for a token string missing from the repository results in an empty response.
     */
    @Test
    @DisplayName("Non-existent token returns an empty Optional")
    void validateRefreshToken_unknownToken_returnsEmpty() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThat(refreshTokenService.validateRefreshToken("unknown")).isEmpty();
    }

    // ── Token Revocation ─────────────────────────────────────────

    /**
     * Verifies that a successful revocation call flags the database state as revoked and commits the changes.
     */
    @Test
    @DisplayName("revokeToken sets revoked=true in the database")
    void revokeToken_setsRevokedTrue() {
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        refreshTokenService.revokeToken("valid-uuid-token");

        assertThat(validToken.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(validToken);
    }

    /**
     * Verifies that attempting to revoke an unknown or missing token terminates gracefully without rasing an exception.
     */
    @Test
    @DisplayName("revokeToken on a non-existent token does not throw an exception")
    void revokeToken_unknownToken_doesNotThrow() {
        when(refreshTokenRepository.findByToken("ghost")).thenReturn(Optional.empty());

        // Should not throw any exception
        refreshTokenService.revokeToken("ghost");
        verify(refreshTokenRepository, never()).save(any());
    }
}