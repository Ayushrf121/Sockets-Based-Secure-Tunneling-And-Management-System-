package com.secureproxy.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void correctPasswordVerifies() {
        String hash = PasswordHasher.hash("S3cret!pass");
        assertTrue(PasswordHasher.verify("S3cret!pass", hash));
    }

    @Test
    void wrongPasswordFails() {
        String hash = PasswordHasher.hash("S3cret!pass");
        assertFalse(PasswordHasher.verify("wrong", hash));
    }

    @Test
    void samePasswordGivesDifferentHashes() {
        assertNotEquals(PasswordHasher.hash("abc12345"), PasswordHasher.hash("abc12345"));
    }

    @Test
    void malformedHashDoesNotThrow() {
        assertFalse(PasswordHasher.verify("abc", "not-a-bcrypt-hash"));
    }
}