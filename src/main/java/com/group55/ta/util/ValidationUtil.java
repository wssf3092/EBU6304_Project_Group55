package com.group55.ta.util;

import java.util.Locale;

/**
 * Input validation and normalization.
 */
public final class ValidationUtil {
    private ValidationUtil() {
    }

    public static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    public static String normalizeEmail(String email) {
        return trim(email).toLowerCase(Locale.ROOT);
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        String normalized = normalizeEmail(email);
        return normalized.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}
