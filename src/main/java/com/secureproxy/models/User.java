package com.secureproxy.models;

/**
 * A registered user. Immutable; use withEnabled() to get a modified copy.
 */
public record User(String username, String passwordHash, Role role, boolean enabled) {

    public enum Role { ADMIN, USER }

    public User withEnabled(boolean newEnabled) {
        return new User(username, passwordHash, role, newEnabled);
    }
}