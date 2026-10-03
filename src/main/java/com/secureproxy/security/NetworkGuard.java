package com.secureproxy.security;

import java.net.InetAddress;

/**
 * Restricts connections to the local network:
 * 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16 and loopback.
 */
public final class NetworkGuard {

    private NetworkGuard() {}

    public static boolean isAllowed(InetAddress address) {
        return address.isLoopbackAddress() || address.isSiteLocalAddress();
    }
}