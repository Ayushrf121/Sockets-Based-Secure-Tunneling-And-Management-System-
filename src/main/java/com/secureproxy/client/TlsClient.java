package com.secureproxy.client;

import com.secureproxy.protocol.Message;
import com.secureproxy.security.TlsContext;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * Milestone 3 client: secure connection, then login, then a small command loop.
 * Trusts only the pinned server certificate.
 */
public class TlsClient {

    private static final int MAX_LOGIN_TRIES = 3;

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.print("Server address (e.g. 127.0.0.1 or 192.168.1.50): ");
        String host = console.nextLine().trim();

        SSLSocketFactory factory =
                TlsContext.clientFactory(Path.of("certs/client-truststore.p12"), TlsContext.passwordFromEnv());

        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, TlsContext.DEFAULT_PORT)) {
            socket.setEnabledProtocols(TlsContext.PROTOCOLS);
            socket.startHandshake(); // fails here if the server's certificate is not the pinned one
            System.out.println("Secure connection: " + socket.getSession().getProtocol()
                    + " / " + socket.getSession().getCipherSuite());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            if (!login(console, in, out)) {
                System.out.println("Not logged in. Disconnecting.");
                return;
            }
            chat(console, in, out);
        }
    }

    private static boolean login(Scanner console, BufferedReader in, PrintWriter out) throws Exception {
        for (int i = 1; i <= MAX_LOGIN_TRIES; i++) {
            System.out.print("Username: ");
            String username = console.nextLine().trim();
            String password = readPassword(console);

            out.println(Message.login(username, password).toJson());
            Message reply = Message.fromJson(in.readLine());

            if (reply == null) {
                System.out.println("Server closed the connection.");
                return false;
            }
            if (Message.LOGIN_OK.equals(reply.type)) {
                System.out.println("Logged in as " + reply.username + " (" + reply.role + ")");
                return true;
            }
            System.out.println("Login failed: " + reply.message);
            if ("LOCKED".equals(reply.code)) return false;
        }
        return false;
    }

    private static void chat(Scanner console, BufferedReader in, PrintWriter out) throws Exception {
        System.out.println("Type text to echo it back, or use /whoami /ping /logout /help");

        while (true) {
            System.out.print("> ");
            String line = console.nextLine().trim();
            if (line.isEmpty()) continue;

            String lower = line.toLowerCase();
            Message request;
            if (lower.equals("/help")) {
                System.out.println("/whoami  show your account   /ping  test the link   /logout  leave");
                continue;
            } else if (lower.equals("/ping")) {
                request = Message.of(Message.PING);
            } else if (lower.equals("/whoami")) {
                request = Message.of(Message.WHOAMI);
            } else if (lower.equals("/logout") || lower.equals("/quit")) {
                request = Message.of(Message.LOGOUT);
            } else if (line.startsWith("/")) {
                System.out.println("Unknown command. Type /help");
                continue;
            } else {
                request = Message.of(Message.ECHO);
                request.text = line;
            }

            out.println(request.toJson());
            Message reply = Message.fromJson(in.readLine());
            if (reply == null) {
                System.out.println("Server closed the connection.");
                return;
            }

            show(reply);
            if (Message.BYE.equals(reply.type)) return;
            if (Message.ERROR.equals(reply.type) && "ACCOUNT_DISABLED".equals(reply.code)) return;
        }
    }

    private static void show(Message reply) {
        if (Message.ECHO.equals(reply.type)) {
            System.out.println("ECHO: " + reply.text);
        } else if (Message.PONG.equals(reply.type)) {
            System.out.println("pong");
        } else if (Message.WHOAMI.equals(reply.type)) {
            System.out.println("You are " + reply.username + " (" + reply.role + ")");
        } else if (Message.BYE.equals(reply.type)) {
            System.out.println("Logged out.");
        } else if (Message.ERROR.equals(reply.type)) {
            System.out.println("Error [" + reply.code + "]: " + reply.message);
        } else {
            System.out.println("Unexpected reply: " + reply.type);
        }
    }

    /** Hidden input when the terminal supports it; otherwise visible (with a warning). */
    private static String readPassword(Scanner console) {
        Console c = System.console();
        if (c != null) {
            char[] pw = c.readPassword("Password: ");
            return pw == null ? "" : new String(pw);
        }
        System.out.print("Password (typing is visible here): ");
        return console.nextLine();
    }
}
