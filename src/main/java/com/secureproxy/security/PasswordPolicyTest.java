package com.secureproxy.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPolicyTest {

    @Test
    void goodPasswordAccepted() {
        assertTrue(PasswordPolicy.violation("alice", "GoodPass123").isEmpty());
    }

    @Test
    void tooShortRejected() {
        assertTrue(PasswordPolicy.violation("alice", "short1").isPresent());
    }

    @Test
    void exactlyMaxBytesAcceptedAndOneMoreRejected() {
        assertTrue(PasswordPolicy.violation("alice", "a".repeat(72)).isEmpty());
        assertTrue(PasswordPolicy.violation("alice", "a".repeat(73)).isPresent());
    }

    @Test
    void sameAsUsernameRejected() {
        assertTrue(PasswordPolicy.violation("alice_smith", "ALICE_SMITH").isPresent());
    }

    @Test
    void controlCharactersRejected() {
        assertTrue(PasswordPolicy.violation("alice", "bad\npassword1").isPresent());
    }

    @Test
    void nullRejected() {
        assertTrue(PasswordPolicy.violation("alice", null).isPresent());
    }
}
