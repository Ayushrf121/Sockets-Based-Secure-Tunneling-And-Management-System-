package com.rhythmcoders.tunnel.proxy;

import java.net.ServerSocket;
import java.net.Socket;

public class ProxyServer extends Thread {
    private final int port;

    public ProxyServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Proxy Server running on port: " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Spawn a dedicated thread for concurrent client handling
                new ClientConnection(clientSocket).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}