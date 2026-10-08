package com.secureproxy.security;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.LongSupplier;

/**
 * Counts failed attempts per key (a username or an IP address) and locks the
 * key for a while after too many failures.
 *
 * - Failures older than the lock duration are forgotten.
 * - A lock ends by itself after the lock duration.
 * - The clock is injectable so tests can move time forward without waiting.
 */
public class LoginLimiter {

    private static final int MAX_ENTRIES = 10_000;

    private static final class Entry {
        int failures;
        long lastFailure;
        long lockedUntil;
    }

    private final int maxFailures;
    private final long lockMillis;
    private final LongSupplier clock;
    private final Map<String, Entry> entries = new HashMap<>();

    public LoginLimiter(int maxFailures, Duration lockDuration) {
        this(maxFailures, lockDuration, System::currentTimeMillis);
    }

    public LoginLimiter(int maxFailures, Duration lockDuration, LongSupplier clock) {
        this.maxFailures = maxFailures;
        this.lockMillis = lockDuration.toMillis();
        this.clock = clock;
    }

    public synchronized boolean isLocked(String key) {
        Entry e = entries.get(key);
        return e != null && e.lockedUntil > clock.getAsLong();
    }

    public synchronized void recordFailure(String key) {
        long now = clock.getAsLong();
        if (entries.size() >= MAX_ENTRIES) purge(now);

        Entry e = entries.computeIfAbsent(key, k -> new Entry());
        if (e.lockedUntil > now) return; // already locked; do not extend the lock

        // A finished lock, or failures older than the window, start the count again
        if (e.lockedUntil != 0 || now - e.lastFailure > lockMillis) {
            e.failures = 0;
            e.lockedUntil = 0;
        }
        e.failures++;
        e.lastFailure = now;
        if (e.failures >= maxFailures) {
            e.lockedUntil = now + lockMillis;
        }
    }

    /** A correct login clears the failure count for that key. */
    public synchronized void recordSuccess(String key) {
        entries.remove(key);
    }

    /** Removes entries that are no longer locked and have no recent failures (keeps memory bounded). */
    private void purge(long now) {
        entries.values().removeIf(e -> e.lockedUntil <= now && now - e.lastFailure > lockMillis);
    }
}
