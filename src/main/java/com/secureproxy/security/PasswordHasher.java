package com.secureproxy.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Bcrypt password hashing. Salt is generated and embedded in the hash by bcrypt.
 * Note: bcrypt only uses the first 72 bytes of a password.
 */
public final class PasswordHasher {

    private static final int COST = 12;

    private PasswordHasher() {}

    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(COST));
    }

    public static boolean verify(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (IllegalArgumentException e) {
            return false; // malformed hash
        }
    }
}