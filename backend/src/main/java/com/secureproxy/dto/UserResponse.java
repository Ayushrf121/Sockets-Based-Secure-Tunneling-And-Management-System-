package com.secureproxy.dto;

import com.secureproxy.model.User;

public class UserResponse {

    private Long id;
    private String name;
    private String role;
    private String status;

    public UserResponse(User user) {

        this.id = user.getId();
        this.name = user.getName();
        this.role = user.getRole();
        this.status = user.getStatus();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }
}