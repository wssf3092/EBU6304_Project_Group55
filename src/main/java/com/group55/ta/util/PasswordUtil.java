package com.group55.ta.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 password hashing (Step 3).
 */
public final class PasswordUtil {
    private static final int SHA256_HEX_LEN = 64;

    private PasswordUtil() {
    }

    /** Same as {@link #sha256(String)}; name matches IMPROVEMENT-PLAN. */
    public static String hash(String plaintext) {
        return sha256(plaintext);
    }

    public static String sha256(String plainText) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    public static boolean verify(String plaintext, String storedHash) {
        if (storedHash == null || plaintext == null) {
            return false;
        }
        if (isLikelySha256Hex(storedHash)) {
            return sha256(ValidationUtil.trim(plaintext)).equals(storedHash);
        }
        // Legacy Step 2 plaintext in passwordHash field
        return plaintext.equals(storedHash);
    }

    static boolean isLikelySha256Hex(String value) {
        if (value == null || value.length() != SHA256_HEX_LEN) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if ((c < '0' || c > '9') && (c < 'a' || c > 'f')) {
                return false;
            }
        }
        return true;
    }
}
