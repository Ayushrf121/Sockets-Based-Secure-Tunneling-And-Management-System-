package com.secureproxy.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CookieDeviceIdHelper {

    private static final String DEVICE_COOKIE =
            "SECURE_PROXY_DEVICE";

    public String getOrCreateDeviceId(
            HttpServletRequest request) {

        if (request.getCookies() != null) {

            for (Cookie cookie : request.getCookies()) {

                if (DEVICE_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return UUID.randomUUID().toString();
    }
}