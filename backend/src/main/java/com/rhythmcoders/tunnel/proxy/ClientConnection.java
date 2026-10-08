package com.rhythmcoders.tunnel.proxy;

import org.mindrot.jbcrypt.BCrypt;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class ClientConnection extends Thread {
    private final Socket clientSocket;
    private final String TARGET_HOST = "93.184.215.14"; 
    private final int TARGET_PORT = 80;

    private String clientId;
    private ClientSession session;

    public ClientConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            InputStream clientIn = clientSocket.getInputStream();
            OutputStream clientOut = clientSocket.getOutputStream();

            // 1. Simple Auth Handshake over the socket stream
            clientOut.write("=== RHYTHM CODERS SECURE PROXY ===\r\nUsername: ".getBytes());
            clientOut.flush();

            Scanner scanner = new Scanner(clientIn);
            if (!scanner.hasNextLine()) return;
            String username = scanner.nextLine().trim();

            clientOut.write("Password: ".getBytes());
            clientOut.flush();

            if (!scanner.hasNextLine()) return;
            String password = scanner.nextLine().trim();

            // 2. Verify credentials using SessionRegistry
            if (!SessionRegistry.verifyUserCredentials(username, password)) {
                clientOut.write("[-] Authentication Failed. Access Denied.\r\n".getBytes());
                clientOut.flush();
                clientSocket.close();
                return;
            }

            // 3. Authentication successful! Register the authenticated client session
            this.clientId = "client_" + System.currentTimeMillis();
            this.session = new ClientSession(clientId, username, clientSocket);
            SessionRegistry.addSession(session);

            clientOut.write("[+] Authentication Successful. Tunnel Established.\r\n\r\n".getBytes());
            clientOut.flush();

            // 4. Begin TCP Tunneling to Target
            try (Socket targetSocket = new Socket(TARGET_HOST, TARGET_PORT);
                 InputStream targetIn = targetSocket.getInputStream();
                 OutputStream targetOut = targetSocket.getOutputStream()) {

                Thread targetToClient = new Thread(() -> {
                    try {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = targetIn.read(buffer)) != -1) {
                            clientOut.write(buffer, 0, bytesRead);
                            clientOut.flush();
                        }
                    } catch (Exception e) {}
                });
                targetToClient.start();

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = clientIn.read(buffer)) != -1) {
                    targetOut.write(buffer, 0, bytesRead);
                    targetOut.flush();
                }
            }

        } catch (Exception e) {
            System.out.println("Connection error or closed.");
        } finally {
            if (clientId != null) {
                SessionRegistry.removeSession(clientId);
            }
        }
    }
}