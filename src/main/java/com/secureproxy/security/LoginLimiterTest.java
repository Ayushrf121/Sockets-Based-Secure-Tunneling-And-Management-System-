package com.secureproxy.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class LoginLimiterTest {

    private AtomicLong now;
    private LoginLimiter limiter;

    @BeforeEach
    void setUp() {
        now = new AtomicLong(1_000_000L);
        limiter = new LoginLimiter(3, Duration.ofMinutes(10), now::get);
    }

    private void fail(String key, int times) {
        for (int i = 0; i < times; i++) limiter.recordFailure(key);
    }

    private void advanceMinutes(long minutes) {
        now.addAndGet(minutes * 60_000L);
    }

    @Test
    void notLockedInitially() {
        assertFalse(limiter.isLocked("user:alice"));
    }

    @Test
    void locksAfterMaxFailures() {
        fail("user:alice", 2);
        assertFalse(limiter.isLocked("user:alice"));
        fail("user:alice", 1);
        assertTrue(limiter.isLocked("user:alice"));
    }

    @Test
    void unlocksAfterLockDuration() {
        fail("user:alice", 3);
        assertTrue(limiter.isLocked("user:alice"));
        advanceMinutes(11);
        assertFalse(limiter.isLocked("user:alice"));
    }

    @Test
    void successClearsFailures() {
        fail("user:alice", 2);
        limiter.recordSuccess("user:alice");
        fail("user:alice", 2);
        assertFalse(limiter.isLocked("user:alice"));
    }

    @Test
    void oldFailuresAreForgotten() {
        fail("user:alice", 2);
        advanceMinutes(11);
        fail("user:alice", 2);
        assertFalse(limiter.isLocked("user:alice"));
    }

    @Test
    void keysAreIndependent() {
        fail("user:alice", 3);
        assertTrue(limiter.isLocked("user:alice"));
        assertFalse(limiter.isLocked("user:bob"));
        assertFalse(limiter.isLocked("ip:192.168.1.5"));
    }
}
