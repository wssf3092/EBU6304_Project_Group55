package com.group55.ta.util;

/**
 * Password complexity rules applied at registration and password change time.
 * <p>
 * Centralized here so {@link com.group55.ta.service.AuthService} stays focused on the auth flow
 * and the same rule can be reused later by an admin "reset password" path.
 */
public final class PasswordPolicy {

    /** Minimum length required for any new password. */
    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    /**
     * Validates a candidate password against the policy.
     *
     * @throws IllegalArgumentException if the password does not satisfy every rule
     */
    public static void validate(String rawPassword) {
        String password = rawPassword == null ? "" : rawPassword;
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must contain at least " + MIN_LENGTH + " characters.");
        }
        if (!containsLetter(password)) {
            throw new IllegalArgumentException("Password must contain at least one letter.");
        }
        if (!containsDigit(password)) {
            throw new IllegalArgumentException("Password must contain at least one digit.");
        }
        if (containsWhitespace(password)) {
            throw new IllegalArgumentException("Password must not contain whitespace characters.");
        }
    }

    private static boolean containsLetter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isLetter(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsDigit(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isDigit(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsWhitespace(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isWhitespace(value.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
