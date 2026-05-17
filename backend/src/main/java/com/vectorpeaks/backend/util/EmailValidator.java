package com.vectorpeaks.backend.util;

/**
 * Validates email addresses.
 * Guards against IDN Homograph Attacks by rejecting non-ASCII characters.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class EmailValidator {

    // simple regex for emails
    private static final String EMAIL_FORMAT = "^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";

    /**
     * Checks if the email contains only ASCII characters and matches the expected format.
     * Rejects Cyrillic, Arabic script, etc., to mitigate IDN Homograph Attacks.
     *
     * @param email the email address to validate
     * @return true if the email is valid and ASCII-only, false otherwise
     */
    public static boolean isValid(String email) {
        if (email == null || email.isBlank()) return false;

        // Reject anything that is not pure ASCII
        for (char c : email.toCharArray()) {
            if (c > 127) return false;
        }

        return email.matches(EMAIL_FORMAT);
    }
}