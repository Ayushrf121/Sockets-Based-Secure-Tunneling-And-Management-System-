package com.rhythmcoders.tunnel.auth;

import org.mindrot.jbcrypt.BCrypt;

public class AuthService {
    private final String storedUsername = "admin";
    private final String storedHash;

    public AuthService(String plainTextPassword) {
        // Hash the password securely upon initialization
        this.storedHash = BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

    public boolean authenticate(String username, String providedPassword) {
        if (!storedUsername.equals(username)) {
            return false;
        }
        // Securely check the provided password against the hash
        return BCrypt.checkpw(providedPassword, storedHash);
    }
}