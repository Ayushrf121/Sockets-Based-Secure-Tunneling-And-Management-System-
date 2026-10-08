package com.rhythmcoders.tunnel.proxy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.util.Map;

public class SessionRegistry {
    private static final Map<String, ClientSession> activeSessions = new ConcurrentHashMap<>();
    private static final Map<String, String> registeredUsers = new ConcurrentHashMap<>(); // username -> bcrypt hash

    public static void addSession(ClientSession session) {
        activeSessions.put(session.getClientId(), session);
    }

    public static void removeSession(String clientId) {
        ClientSession session = activeSessions.remove(clientId);
        if (session != null) {
            session.terminate();
        }
    }

    public static Collection<ClientSession> getActiveSessions() {
        return activeSessions.values();
    }

    public static boolean registerUser(String username, String passwordHash) {
        if (registeredUsers.containsKey(username)) return false;
        registeredUsers.put(username, passwordHash);
        return true;
    }

    public static boolean verifyUser(String username, String password) {
        // For simplicity in PBL demonstration
        return registeredUsers.containsKey(username);
    }
}