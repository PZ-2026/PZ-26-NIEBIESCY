// EmailValidatorTest.java
package com.vectorpeaks.backend.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EmailValidator}.
 *
 * Verifies that standard ASCII emails are accepted and that
 * IDN Homograph Attack attempts (non-ASCII characters) are rejected.
 *
 * @version 1.0
 * @author EduLink Team
 */
class EmailValidatorTest {

    // ── Valid Emails ─────────────────────────────────────────────

    @ParameterizedTest(name = "Valid email: {0}")
    @ValueSource(strings = {
            "user@example.com",
            "admin@edulink.pl",
            "jan.kowalski+tag@firma.co.uk",
            "user123@sub.domain.com"
    })
    @DisplayName("Valid ASCII emails are accepted")
    void validAsciiEmails_areAccepted(String email) {
        assertThat(EmailValidator.isValid(email)).isTrue();
    }

    // ── IDN Homograph Attack ─────────────────────────────────────

    @Test
    @DisplayName("Email with Cyrillic characters (IDN Homograph) is rejected")
    void cyrillicEmail_isRejected() {
        // 'а' is Cyrillic U+0430, visually identical to Latin 'a'
        String homographEmail = "\u0430dmin@example.com";
        assertThat(EmailValidator.isValid(homographEmail)).isFalse();
    }

    @ParameterizedTest(name = "Non-ASCII email: {0}")
    @ValueSource(strings = {
            "użytkownik@example.com",   // Polish letter 'ż'
            "аdmin@example.com",         // Cyrillic 'а'
            "admin@exämple.com",         // German 'ä'
            "用户@example.com"            // Chinese characters
    })
    @DisplayName("Non-ASCII emails are rejected (IDN Homograph protection)")
    void nonAsciiEmails_areRejected(String email) {
        assertThat(EmailValidator.isValid(email)).isFalse();
    }

    // ── Invalid Format ───────────────────────────────────────────

    @ParameterizedTest(name = "Invalid format: {0}")
    @ValueSource(strings = {
            "not-an-email",
            "@example.com",
            "user@",
            "",
            "user @example.com"
    })
    @DisplayName("Emails with invalid format are rejected")
    void invalidFormatEmails_areRejected(String email) {
        assertThat(EmailValidator.isValid(email)).isFalse();
    }

    @Test
    @DisplayName("Null is rejected without throwing an exception")
    void nullEmail_isRejected() {
        assertThat(EmailValidator.isValid(null)).isFalse();
    }
}