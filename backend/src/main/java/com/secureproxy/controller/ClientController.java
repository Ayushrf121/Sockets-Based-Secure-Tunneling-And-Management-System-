package com.secureproxy.controller;

import com.secureproxy.dto.ClientRequest;
import com.secureproxy.dto.UserResponse;
import com.secureproxy.model.User;
import com.secureproxy.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final UserService userService;

    public ClientController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/request-access")
    public ResponseEntity<UserResponse> requestAccess(
            @RequestBody ClientRequest request) {

        User user = userService.registerClient(request);

        return ResponseEntity.ok(
                new UserResponse(user)
        );
    }
}