package com.secureproxy.security;

import com.secureproxy.model.Session;
import com.secureproxy.model.User;
import com.secureproxy.service.SessionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;
    private final CookieService cookieService;

    public AuthInterceptor(
            SessionService sessionService,
            CookieService cookieService) {

        this.sessionService = sessionService;
        this.cookieService = cookieService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String uri = request.getRequestURI();

        // Public endpoints
        if (uri.equals("/api/auth/admin/login")
                || uri.equals("/api/auth/client/login")
                || uri.equals("/api/auth/logout")
                || uri.equals("/api/client/request-access")
                || uri.equals("/api/test")) {

            return true;
        }

        // Only protect our API
        if (!uri.startsWith("/api/")) {
            return true;
        }

        UUID sessionId =
                cookieService.getSessionId(request);

        if (sessionId == null) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            return false;
        }

        Session session =
                sessionService.getValidSession(sessionId);

        if (session == null) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            return false;
        }

        User user = session.getUser();

        if (user == null) {
            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED
            );
            return false;
        }

        if (user.getStatus().equals("REMOVED")
                || user.getStatus().equals("BLOCKED")) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return false;
        }

        // Admin endpoints require ADMIN
        if (uri.startsWith("/api/admin/")
                && !user.getRole().equals("ADMIN")) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN
            );
            return false;
        }

        // Put authenticated data into request
        request.setAttribute("user", user);
        request.setAttribute("session", session);

        return true;
    }
}