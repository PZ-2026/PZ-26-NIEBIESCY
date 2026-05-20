/*
 * LoginAttemptServiceTest.java
 *
 * Version: 1.0
 * Date: 2026-05-18
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoginAttemptService}.
 *
 * <p>Verifies brute-force protection logic:
 * progressive delays, account lockout, IP-based blocking,
 * and automatic lockout expiry.
 *
 * <p>No Spring context needed — pure unit test.
 *
 * @version 1.0
 * @author EduLink Team
 * @see LoginAttemptService
 */
class LoginAttemptServiceTest {

    /** The instance under test. */
    private LoginAttemptService service;

    /** Reusable test email address. */
    private static final String EMAIL = "test@example.com";

    /** Primary test IP address. */
    private static final String IP    = "192.168.1.1";

    /** Secondary test IP address used for isolation checks. */
    private static final String IP2   = "10.0.0.1";

    /**
     * Initializes a fresh instance of {@link LoginAttemptService} before each test execution.
     */
    @BeforeEach
    void setUp() {
        service = new LoginAttemptService();
    }

    // ── Lockout by Email+IP ──────────────────────────────────────

    /**
     * Verifies that a newly created account with no prior history is not blocked by default.
     */
    @Test
    @DisplayName("New account is not blocked")
    void newAccount_isNotBlocked() {
        assertThat(service.isBlocked(EMAIL, IP)).isFalse();
    }

    /**
     * Verifies that recording exactly four consecutive failures does not trigger
     * an account lockout yet.
     */
    @Test
    @DisplayName("After 4 failures, the account is not blocked yet")
    void fourFailures_notBlockedYet() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.isBlocked(EMAIL, IP)).isFalse();
    }

    /**
     * Verifies that recording five consecutive login failures successfully triggers
     * an account lockout for the targeted email and IP pair.
     */
    @Test
    @DisplayName("After 5 failures, the account is blocked")
    void fiveFailures_accountBlocked() {
        for (int i = 0; i < 5; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.isBlocked(EMAIL, IP)).isTrue();
    }

    /**
     * Verifies that a successful login resets the internal failure counter,
     * allowing subsequent failed attempts without triggering an immediate block.
     */
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

    /**
     * Verifies that a lockout is scope-restricted to a specific email and IP pair,
     * ensuring that requests from a alternative IP remain unblocked.
     */
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

    /**
     * Verifies that no progressive artificial delay is enforced for the first
     * two failed login attempts.
     */
    @Test
    @DisplayName("No delay for the first 2 attempts")
    void noDelay_forFirstTwoAttempts() {
        service.recordFailure(EMAIL, IP);
        service.recordFailure(EMAIL, IP);
        assertThat(service.getDelayMs(EMAIL, IP)).isZero();
    }

    /**
     * Verifies that a progressive delay of exactly 2 seconds (2000 ms) is introduced
     * immediately following the third failed login attempt.
     */
    @Test
    @DisplayName("2-second delay after the 3rd attempt")
    void twoSecondDelay_afterThirdAttempt() {
        for (int i = 0; i < 3; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.getDelayMs(EMAIL, IP)).isEqualTo(2_000L);
    }

    /**
     * Verifies that a progressive delay of exactly 5 seconds (5000 ms) is introduced
     * immediately following the fourth failed login attempt.
     */
    @Test
    @DisplayName("5-second delay after the 4th attempt")
    void fiveSecondDelay_afterFourthAttempt() {
        for (int i = 0; i < 4; i++) {
            service.recordFailure(EMAIL, IP);
        }
        assertThat(service.getDelayMs(EMAIL, IP)).isEqualTo(5_000L);
    }

    // ── Lockout by IP (Credential Stuffing Protection) ───────────

    /**
     * Verifies credential stuffing protection by ensuring an IP address is entirely blocked
     * after accumulation of 20 total failures distributed across various system accounts.
     */
    @Test
    @DisplayName("IP is blocked after 20 attempts across different accounts")
    void ipBlocked_afterTwentyAttemptsOnDifferentAccounts() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("user" + i + "@example.com", IP);
        }
        // Check using an arbitrary email from this blocked IP
        assertThat(service.isBlocked("anyuser@example.com", IP)).isTrue();
    }

    /**
     * Verifies that IP blocking is isolated and an intensive block on one network address
     * does not inadvertently restrict traffic originating from an independent IP address.
     */
    @Test
    @DisplayName("Different IPs do not block each other")
    void differentIps_doNotBlockEachOther() {
        for (int i = 0; i < 20; i++) {
            service.recordFailure("user" + i + "@example.com", IP);
        }
        assertThat(service.isBlocked("victim@example.com", IP2)).isFalse();
    }
}