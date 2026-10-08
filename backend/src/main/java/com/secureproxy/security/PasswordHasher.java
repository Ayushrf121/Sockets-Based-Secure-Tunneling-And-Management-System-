package com.secureproxy.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {

    private final BCryptPasswordEncoder encoder;

    public PasswordHasher() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String hash(String password) {

        return encoder.encode(password);
    }

    public boolean matches(String password, String passwordHash) {

        return encoder.matches(password, passwordHash);
    }
}