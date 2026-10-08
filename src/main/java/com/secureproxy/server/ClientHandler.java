package com.secureproxy.server;

import com.secureproxy.models.User;
import com.secureproxy.protocol.BoundedLineReader;
import com.secureproxy.protocol.Message;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Runs one client connection from handshake to disconnect.
 *
 * Phases:
 *   1. TLS handshake      (10 s limit)
 *   2. Login              (30 s limit, at most 3 tries on this connection)
 *   3. Session            (5 minutes of silence ends it)
 *
 * IMPORTANT: raw lines are never logged, because a LOGIN line contains the password.
 */
public class ClientHandler implements Runnable {

    private static final int HANDSHAKE_TIMEOUT_MS = 10_000;
    private static final int LOGIN_TIMEOUT_MS = 30_000;
    private static final int IDLE_TIMEOUT_MS = 5 * 60 * 1000;
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final int MAX_LINE_CHARS = 4096;

    private final Socket socket;
    private final AuthService auth;

    public ClientHandler(Socket socket, AuthService auth) {
        this.socket = socket;
        this.auth = auth;
    }

    @Override
    public void run() {
        String ip = socket.getInetAddress().getHostAddress();
        try (Socket s = socket) {
            SSLSocket ssl = (SSLSocket) s;
            ssl.setSoTimeout(HANDSHAKE_TIMEOUT_MS);
            ssl.startHandshake();

            BoundedLineReader in = new BoundedLineReader(
                    new InputStreamReader(ssl.getInputStream(), StandardCharsets.UTF_8), MAX_LINE_CHARS);
            PrintWriter out = new PrintWriter(ssl.getOutputStream(), true, StandardCharsets.UTF_8);

            User user = login(ssl, in, out, ip);
            if (user != null) {
                session(ssl, in, out, user);
                System.out.println("[SERVER] " + safe(user.username()) + " disconnected (" + ip + ")");
            }
        } catch (IOException e) {
            System.out.println("[SERVER] Connection ended (" + ip + "): " + e.getMessage());
        }
    }

    /** @return the logged-in user, or null if login failed and the connection should close */
    private User login(SSLSocket ssl, BoundedLineReader in, PrintWriter out, String ip) throws IOException {
        ssl.setSoTimeout(LOGIN_TIMEOUT_MS);

        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            String line = in.readLine();
            if (line == null) return null;

            Message m = Message.fromJson(line);
            if (m == null) {
                send(out, Message.error("BAD_REQUEST", "Invalid message"));
                return null;
            }
            if (!Message.LOGIN.equals(m.type)) {
                send(out, Message.error("LOGIN_REQUIRED", "Please log in first"));
                continue;
            }

            AuthService.Result result = auth.login(m.username, m.password, ip);
            switch (result.status()) {
                case OK -> {
                    Message ok = Message.of(Message.LOGIN_OK);
                    ok.username = result.user().username();
                    ok.role = result.user().role().name();
                    send(out, ok);
                    System.out.println("[AUTH] Login OK: " + safe(ok.username) + " from " + ip);
                    return result.user();
                }
                case LOCKED -> {
                    send(out, Message.error("LOCKED", "Too many failed attempts. Try again later."));
                    System.out.println("[AUTH] Blocked (locked) attempt for '" + safe(m.username) + "' from " + ip);
                    return null;
                }
                case INVALID -> {
                    send(out, Message.error("AUTH_FAILED", "Invalid username or password"));
                    System.out.println("[AUTH] Login FAILED for '" + safe(m.username) + "' from " + ip);
                }
            }
        }

        send(out, Message.error("TOO_MANY_ATTEMPTS", "Too many attempts on this connection"));
        return null;
    }

    private void session(SSLSocket ssl, BoundedLineReader in, PrintWriter out, User user) throws IOException {
        ssl.setSoTimeout(IDLE_TIMEOUT_MS);

        while (true) {
            String line = in.readLine();
            if (line == null) return;

            // Re-check on every message, so disabling or removing a user ends their session
            Optional<User> current = auth.activeUser(user.username());
            if (current.isEmpty()) {
                send(out, Message.error("ACCOUNT_DISABLED", "Your account is no longer active"));
                System.out.println("[AUTH] Ended session of disabled/removed user " + safe(user.username()));
                return;
            }

            Message m = Message.fromJson(line);
            if (m == null) {
                send(out, Message.error("BAD_REQUEST", "Invalid message"));
                continue;
            }

            switch (m.type) {
                case Message.PING -> send(out, Message.of(Message.PONG));
                case Message.WHOAMI -> {
                    Message reply = Message.of(Message.WHOAMI);
                    reply.username = current.get().username();
                    reply.role = current.get().role().name();
                    send(out, reply);
                }
                case Message.ECHO -> {
                    Message reply = Message.of(Message.ECHO);
                    reply.text = m.text;
                    send(out, reply);
                }
                case Message.LOGOUT -> {
                    send(out, Message.of(Message.BYE));
                    return;
                }
                case Message.LOGIN -> send(out, Message.error("ALREADY_LOGGED_IN", "You are already logged in"));
                default -> send(out, Message.error("UNKNOWN_TYPE", "Unknown message type"));
            }
        }
    }

    private static void send(PrintWriter out, Message m) {
        out.println(m.toJson());
    }

    /** Makes text safe to print in logs: shortens it and replaces odd characters (prevents fake log lines). */
    private static String safe(String s) {
        if (s == null) return "null";
        String cut = s.length() > 32 ? s.substring(0, 32) : s;
        return cut.replaceAll("[^A-Za-z0-9_.-]", "?");
    }
}
