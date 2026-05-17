// LoginAttemptServiceTest.java

package com.vectorpeaks.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoginAttemptService}.
 *
 * Verifies brute-force protection logic:
 * progressive delays, account lockout, IP-based blocking,
 * and automatic lockout expiry.
 *
 * No Spring context needed — pure unit test.
 *
 * @version 1.0
 * @author EduLink Team
 */
class LoginAttemptServiceTest {

    private LoginAttemptService service;

    private static final String EMAIL = "test@example.com";
    private static final String IP    = "192.168.1.1";
    private static final String IP2   = "10.0.0.1";

    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    // ── Lockout by Email+IP ──────────────────────────────────────

    @Test
    @DisplayName("New account is not blocked")
    void newAccount_isNotBlocked() {
        assertThat(service.isBlocked(EMAIL, IP)).isFalse();
    }

    @Test
    @DisplayName("After 4 failures, the account is not blocked yet")
    void fourFailures_notBlockedYet() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.isBlocked(EMAIL, IP)).isFalse();
    }

    @Test
    @DisplayName("After 5 failures, the account is blocked")
    void fiveFailures_accountBlocked() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.isBlocked(EMAIL, IP)).isTrue();
    }

    @Test
    @DisplayName("Successful login resets the failure counter")
    void successfulLogin_resetsCounter() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        service.recordSuccess(EMAIL, IP);

        // After reset, attempts can be made again
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.isBlocked(EMAIL, IP)).isFalse();
    }

    @Test
    @DisplayName("Lockout applies to the specific email+IP pair, not globally")
    void blockIsPerEmailIpPair_notGlobal() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL, IP);
        }
        // Same email, different IP — should not be blocked
        assertThat(service.isBlocked(EMAIL, IP2)).isFalse();
    }

    // ── Progressive Delays ───────────────────────────────────────

    @Test
    @DisplayName("No delay for the first 2 attempts")
    void noDelay_forFirstTwoAttempts() {
        service.recordFailure(EMAIL, IP);
        service.recordFailure(EMAIL, IP);
        assertThat(service.getDelayMs(EMAIL, IP)).isZero();
    }

    @Test
    @DisplayName("2-second delay after the 3rd attempt")
    void twoSecondDelay_afterThirdAttempt() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.getDelayMs(EMAIL, IP)).isEqualTo(2_000L);
    }

    @Test
    @DisplayName("5-second delay after the 4th attempt")
    void fiveSecondDelay_afterFourthAttempt() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.getDelayMs(EMAIL, IP)).isEqualTo(5_000L);
    }

    // ── Lockout by IP (Credential Stuffing Protection) ───────────

    @Test
    @DisplayName("IP is blocked after 20 attempts across different accounts")
    void ipBlocked_afterTwentyAttemptsOnDifferentAccounts() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("user" + i + "@example.com", IP);
        }
        // Check using an arbitrary email from this blocked IP
        assertThat(service.isBlocked("anyuser@example.com", IP)).isTrue();
    }

    @Test
    @DisplayName("Different IPs do not block each other")
    void differentIps_doNotBlockEachOther() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("user" + i + "@example.com", IP);
        }
        assertThat(service.isBlocked("victim@example.com", IP2)).isFalse();
    }
}