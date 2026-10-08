package com.rhythmcoders.tunnel.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ClientConnection extends Thread {
    private final Socket clientSocket;
    
    // Target destination (testing with example.com IP)
    private final String TARGET_HOST = "93.184.215.14"; 
    private final int TARGET_PORT = 80;

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try (Socket targetSocket = new Socket(TARGET_HOST, TARGET_PORT);
             InputStream clientIn = clientSocket.getInputStream();
             OutputStream clientOut = clientSocket.getOutputStream();
             InputStream targetIn = targetSocket.getInputStream();
             OutputStream targetOut = targetSocket.getOutputStream()) {

            // Sub-thread: Target Server -> Client Stream
            Thread targetToClient = new Thread(() -> {
                try {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = targetIn.read(buffer)) != -1) {
                        clientOut.write(buffer, 0, bytesRead);
                        clientOut.flush();
                    }
                } catch (Exception e) {
                    // Stream closed normally
                }
            });
            targetToClient.start();

            // Main thread: Client -> Target Server Stream
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = clientIn.read(buffer)) != -1) {
                targetOut.write(buffer, 0, bytesRead);
                targetOut.flush();
            }

        } catch (Exception e) {
            System.out.println("Tunnel connection closed.");
        }
    }
}