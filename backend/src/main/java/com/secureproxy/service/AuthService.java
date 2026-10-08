package com.secureproxy.service;

import com.secureproxy.dto.LoginRequest;
import com.secureproxy.model.Session;
import com.secureproxy.model.User;
import com.secureproxy.repository.UserRepository;
import com.secureproxy.security.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final SessionService sessionService;

    public AuthService(
            UserRepository userRepository,
            PasswordHasher passwordHasher,
            SessionService sessionService) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.sessionService = sessionService;
    }

    public Session login(
            LoginRequest request,
            String deviceId,
            String ipAddress,
            String requiredRole) {

        if (request.getName() == null ||
                request.getName().isBlank()) {

            throw new IllegalArgumentException(
                    "Name is required"
            );
        }

        if (request.getPassword() == null ||
                request.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }

        User user = userRepository
                .findByNameAndRole(
                        request.getName().trim(),
                        requiredRole
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid username or password"
                        ));

        if (!passwordHasher.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new IllegalArgumentException(
                    "Invalid username or password"
            );
        }

        if (user.getStatus().equals("REMOVED") ||
                user.getStatus().equals("BLOCKED")) {

            throw new IllegalArgumentException(
                    "Account is not allowed to login"
            );
        }

        if (requiredRole.equals("CLIENT") &&
                !user.getStatus().equals("ACTIVE")) {

            throw new IllegalArgumentException(
                    "Client access has not been approved"
            );
        }

        Session session = sessionService.createSession(
                user,
                deviceId,
                ipAddress
        );

        return session;
    }
}