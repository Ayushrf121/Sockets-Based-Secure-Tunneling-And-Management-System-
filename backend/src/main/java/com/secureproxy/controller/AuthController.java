package com.secureproxy.controller;

import com.secureproxy.dto.AuthResponse;
import com.secureproxy.dto.LoginRequest;
import com.secureproxy.dto.UserResponse;
import com.secureproxy.model.Session;
import com.secureproxy.model.User;
import com.secureproxy.security.CookieService;
import com.secureproxy.service.AuthService;
import com.secureproxy.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SessionService sessionService;
    private final CookieService cookieService;

    public AuthController(
            AuthService authService,
            SessionService sessionService,
            CookieService cookieService) {

        this.authService = authService;
        this.sessionService = sessionService;
        this.cookieService = cookieService;
    }

    @PostMapping("/client/login")
    public ResponseEntity<AuthResponse> clientLogin(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Session session = authService.login(
                request,
                getDeviceId(httpRequest),
                httpRequest.getRemoteAddr(),
                "CLIENT"
        );

        cookieService.addSessionCookie(
                httpResponse,
                session.getId()
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        "Login successful",
                        new UserResponse(session.getUser())
                )
        );
    }

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        Session session = authService.login(
                request,
                getDeviceId(httpRequest),
                httpRequest.getRemoteAddr(),
                "ADMIN"
        );

        cookieService.addSessionCookie(
                httpResponse,
                session.getId()
        );

        return ResponseEntity.ok(
                new AuthResponse(
                        "Admin login successful",
                        new UserResponse(session.getUser())
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            HttpServletRequest request) {

        User user =
                (User) request.getAttribute("user");

        return ResponseEntity.ok(
                new UserResponse(user)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        var sessionId =
                cookieService.getSessionId(request);

        if (sessionId != null) {
            sessionService.revokeSession(sessionId);
        }

        cookieService.clearSessionCookie(response);

        return ResponseEntity.ok(
                "Logged out successfully"
        );
    }

    private String getDeviceId(
            HttpServletRequest request) {

        if (request.getCookies() != null) {

            for (var cookie : request.getCookies()) {

                if ("SECURE_PROXY_DEVICE"
                        .equals(cookie.getName())) {

                    return cookie.getValue();
                }
            }
        }

        return "device-" + request.getRemoteAddr();
    }
}