package com.secureproxy.security;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Rules for NEW passwords (checked when an admin creates a user or changes a password).
 *
 * The 72-byte maximum exists because bcrypt ignores everything after the first
 * 72 bytes. Without this rule, two different long passwords could silently
 * behave as the same password.
 */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_BYTES = 72;

    private PasswordPolicy() {}

    /** @return a message describing the problem, or empty if the password is acceptable */
    public static Optional<String> violation(String username, String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return Optional.of("Password must be at least " + MIN_LENGTH + " characters");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            return Optional.of("Password must be at most " + MAX_BYTES + " bytes (about 72 normal characters)");
        }
        if (password.chars().anyMatch(Character::isISOControl)) {
            return Optional.of("Password must not contain control characters");
        }
        if (username != null && password.equalsIgnoreCase(username)) {
            return Optional.of("Password must not be the same as the username");
        }
        return Optional.empty();
    }
}
