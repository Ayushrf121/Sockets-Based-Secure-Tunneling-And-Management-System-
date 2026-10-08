package com.rhythmcoders.tunnel.proxy;

import java.net.Socket;
import java.time.Instant;

public class ClientSession {
    private final String clientId;
    private final String username;
    private final Socket socket;
    private final Instant connectedAt;

    public ClientSession(String clientId, String username, Socket socket) {
        this.clientId = clientId;
        this.username = username;
        this.socket = socket;
        this.connectedAt = Instant.now();
    }

    public String getClientId() { return clientId; }
    public String getUsername() { return username; }
    public Socket getSocket() { return socket; }
    public Instant getConnectedAt() { return connectedAt; }

    public void terminate() {
        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}