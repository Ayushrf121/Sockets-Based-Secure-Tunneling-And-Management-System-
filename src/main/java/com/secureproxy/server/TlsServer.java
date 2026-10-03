package com.secureproxy.server;

import com.secureproxy.security.NetworkGuard;
import com.secureproxy.security.TlsContext;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Milestone 2: encrypted echo server. Login comes in Milestone 3. */
public class TlsServer {

    private static final int PORT = 5443;
    private static final int IDLE_TIMEOUT_MS = 5 * 60 * 1000;

    public static void main(String[] args) throws Exception {
        SSLServerSocketFactory factory =
                TlsContext.serverFactory(Path.of("certs/server.p12"), TlsContext.passwordFromEnv());
        ExecutorService pool = Executors.newFixedThreadPool(20);

        try (SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(PORT)) {
            server.setEnabledProtocols(TlsContext.PROTOCOLS);
            System.out.println("[SERVER] TLS 1.3 listening on port " + PORT);

            while (true) {
                Socket client = server.accept();
                if (!NetworkGuard.isAllowed(client.getInetAddress())) {
                    System.out.println("[SERVER] Rejected non-local address: " + client.getRemoteSocketAddress());
                    client.close();
                    continue;
                }
                pool.execute(() -> handle(client));
            }
        }
    }

    private static void handle(Socket socket) {
        String who = String.valueOf(socket.getRemoteSocketAddress());
        System.out.println("[SERVER] Connected: " + who);
        try (socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

            socket.setSoTimeout(IDLE_TIMEOUT_MS);
            String line;
            while ((line = in.readLine()) != null) {
                out.println("ECHO: " + line);
                if (line.trim().equalsIgnoreCase("QUIT")) break;
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Connection error (" + who + "): " + e.getMessage());
        }
        System.out.println("[SERVER] Disconnected: " + who);
    }
}