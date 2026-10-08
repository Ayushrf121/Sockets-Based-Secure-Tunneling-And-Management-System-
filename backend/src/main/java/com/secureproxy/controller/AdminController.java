package com.secureproxy.controller;

import com.secureproxy.dto.UserResponse;
import com.secureproxy.model.User;
import com.secureproxy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/clients/pending")
    public ResponseEntity<List<UserResponse>> getPendingClients() {

        List<UserResponse> clients = userService
                .getPendingUsers()
                .stream()
                .map(UserResponse::new)
                .toList();

        return ResponseEntity.ok(clients);
    }

    @PostMapping("/clients/{id}/approve")
    public ResponseEntity<UserResponse> approveClient(
            @PathVariable Long id) {

        User user = userService.approveUser(id);

        return ResponseEntity.ok(
                new UserResponse(user));
    }

    @PostMapping("/clients/{id}/reject")
    public ResponseEntity<UserResponse> rejectClient(
            @PathVariable Long id) {

        User user = userService.rejectUser(id);

        return ResponseEntity.ok(
                new UserResponse(user));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard() {

        var clients = userService.getAllClients();

        long active = clients.stream()
                .filter(u -> u.getStatus().equals("ACTIVE"))
                .count();

        long pending = clients.stream()
                .filter(u -> u.getStatus().equals("PENDING"))
                .count();

        return ResponseEntity.ok(
                java.util.Map.of(
                        "totalClients", clients.size(),
                        "activeClients", active,
                        "pendingClients", pending));
    }

    @DeleteMapping("/clients/{id}")
    public ResponseEntity<UserResponse> removeClient(
            @PathVariable Long id) {

        User user = userService.removeUser(id);

        return ResponseEntity.ok(
                new UserResponse(user));
    }
}