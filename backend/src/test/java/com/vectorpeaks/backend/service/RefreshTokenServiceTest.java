// RefreshTokenServiceTest.java
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
 * Verifies token creation, validation (expired/revoked),
 * and revocation logic.
 *
 * @version 1.0
 * @author EduLink Team
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private RefreshToken validToken;

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

    @Test
    @DisplayName("Valid token is accepted")
    void validateRefreshToken_validToken_returnsPresent() {
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isPresent();
    }

    @Test
    @DisplayName("Revoked token is rejected")
    void validateRefreshToken_revokedToken_returnsEmpty() {
        validToken.setRevoked(true);
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isEmpty();
    }

    @Test
    @DisplayName("Expired token is rejected")
    void validateRefreshToken_expiredToken_returnsEmpty() {
        validToken.setExpiresAt(Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findByToken("valid-uuid-token"))
                .thenReturn(Optional.of(validToken));

        assertThat(refreshTokenService.validateRefreshToken("valid-uuid-token"))
                .isEmpty();
    }

    @Test
    @DisplayName("Non-existent token returns an empty Optional")
    void validateRefreshToken_unknownToken_returnsEmpty() {
        when(refreshTokenRepository.findByToken("unknown")).thenReturn(Optional.empty());

        assertThat(refreshTokenService.validateRefreshToken("unknown")).isEmpty();
    }

    // ── Token Revocation ─────────────────────────────────────────

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

    @Test
    @DisplayName("revokeToken on a non-existent token does not throw an exception")
    void revokeToken_unknownToken_doesNotThrow() {
        when(refreshTokenRepository.findByToken("ghost")).thenReturn(Optional.empty());

        // Should not throw any exception
        refreshTokenService.revokeToken("ghost");
        verify(refreshTokenRepository, never()).save(any());
    }
}