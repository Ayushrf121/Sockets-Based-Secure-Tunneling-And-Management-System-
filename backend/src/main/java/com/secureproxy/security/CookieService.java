package com.secureproxy.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CookieService {

    public static final String SESSION_COOKIE =
            "SECURE_PROXY_SESSION";

    public void addSessionCookie(
            HttpServletResponse response,
            UUID sessionId) {

        Cookie cookie = new Cookie(
                SESSION_COOKIE,
                sessionId.toString()
        );

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(60 * 60);

        response.addCookie(cookie);
    }

    public UUID getSessionId(
            HttpServletRequest request) {

        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {

            if (SESSION_COOKIE.equals(cookie.getName())) {

                try {
                    return UUID.fromString(
                            cookie.getValue()
                    );
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }

        return null;
    }

    public void clearSessionCookie(
            HttpServletResponse response) {

        Cookie cookie = new Cookie(
                SESSION_COOKIE,
                ""
        );

        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}