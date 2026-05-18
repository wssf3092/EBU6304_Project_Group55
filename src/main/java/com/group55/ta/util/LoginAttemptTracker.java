package com.group55.ta.util;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory tracker that locks a login key (normalized email) for a fixed window after too many
 * consecutive authentication failures.
 * <p>
 * Intentionally process-local: the system has no shared cache, and a TA recruitment app does
 * not need cross-node coordination. State resets on JVM restart, which is acceptable for the
 * brute-force scenarios we care about.
 */
public final class LoginAttemptTracker {

    /** Number of consecutive failures that triggers a lockout. */
    public static final int MAX_FAILURES = 5;

    /** Length of the lockout window once {@link #MAX_FAILURES} is exceeded. */
    public static final Duration LOCKOUT_WINDOW = Duration.ofMinutes(15);

    private static final Map<String, AttemptState> STATES = new ConcurrentHashMap<>();

    private LoginAttemptTracker() {
    }

    /**
     * @throws IllegalStateException if the key is currently locked out
     */
    public static void ensureNotLocked(String key) {
        AttemptState state = STATES.get(normalize(key));
        if (state == null) {
            return;
        }
        if (state.lockedUntil != null && Instant.now().isBefore(state.lockedUntil)) {
            long remaining = Duration.between(Instant.now(), state.lockedUntil).toMinutes() + 1;
            throw new IllegalStateException("Too many failed attempts. Try again in " + remaining + " minute(s).");
        }
    }

    /** Records a failed attempt and applies the lockout when the threshold is reached. */
    public static void recordFailure(String key) {
        String normalized = normalize(key);
        STATES.compute(normalized, (k, existing) -> {
            AttemptState state = existing == null ? new AttemptState() : existing;
            state.failures++;
            if (state.failures >= MAX_FAILURES) {
                state.lockedUntil = Instant.now().plus(LOCKOUT_WINDOW);
                state.failures = 0;
            }
            return state;
        });
    }

    /** Clears any failure counters and lockout for the key after a successful login. */
    public static void recordSuccess(String key) {
        STATES.remove(normalize(key));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase();
    }

    private static final class AttemptState {
        int failures;
        Instant lockedUntil;
    }
}
