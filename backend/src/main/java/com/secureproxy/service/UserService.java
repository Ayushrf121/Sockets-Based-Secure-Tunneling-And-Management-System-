package com.secureproxy.service;

import com.secureproxy.dto.ClientRequest;
import com.secureproxy.model.User;
import com.secureproxy.repository.UserRepository;
import com.secureproxy.security.PasswordHasher;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public java.util.List<User> getPendingUsers() {

        return userRepository.findByRoleAndStatus(
                "CLIENT",
                "PENDING");
    }

    public User removeUser(Long id) {

        User user = getUserById(id);

        user.setStatus("REMOVED");
        user.setRemovedAt(
                java.time.LocalDateTime.now());

        return userRepository.save(user);
    }

    public java.util.List<User> getAllClients() {

        return userRepository.findByRole("CLIENT");
    }

    public User getUserByName(String name) {

        return userRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found"));
    }

    public UserService(
            UserRepository userRepository,
            PasswordHasher passwordHasher) {

        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public User approveUser(Long id) {

        User user = getUserById(id);

        if (!user.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException(
                    "Only pending users can be approved");
        }

        user.setStatus("ACTIVE");

        return userRepository.save(user);
    }

    public User rejectUser(Long id) {

        User user = getUserById(id);

        if (!user.getStatus().equals("PENDING")) {
            throw new IllegalArgumentException(
                    "Only pending users can be rejected");
        }

        user.setStatus("REJECTED");

        return userRepository.save(user);
    }

    public User registerClient(ClientRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        if (userRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("User already exists");
        }

        String passwordHash = passwordHasher.hash(request.getPassword());

        User user = new User(
                request.getName(),
                passwordHash);

        user.setRole("CLIENT");
        user.setStatus("PENDING");

        return userRepository.save(user);
    }
}