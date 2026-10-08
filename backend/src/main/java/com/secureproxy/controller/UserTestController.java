package com.secureproxy.controller;

import com.secureproxy.model.User;
import com.secureproxy.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test/users")
public class UserTestController {

    private final UserService userService;

    public UserTestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {

        return userService.saveUser(user);
    }

    @GetMapping
    public List<User> getUsers() {

        return userService.getAllUsers();
    }
}