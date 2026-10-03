package com.secureproxy.client;

import com.secureproxy.security.TlsContext;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Scanner;

/** Milestone 2: encrypted echo client. */
public class TlsClient {

    public static void main(String[] args) throws Exception {
        Scanner console = new Scanner(System.in);
        System.out.print("Server address (e.g. 127.0.0.1 or 192.168.1.50): ");
        String host = console.nextLine().trim();
        int port = 5443;

        SSLSocketFactory factory =
                TlsContext.clientFactory(Path.of("certs/client-truststore.p12"), TlsContext.passwordFromEnv());

        try (SSLSocket socket = (SSLSocket) factory.createSocket(host, port)) {
            socket.setEnabledProtocols(TlsContext.PROTOCOLS);
            socket.startHandshake(); // fails here if the server's certificate isn't trusted
            System.out.println("Secure connection: " + socket.getSession().getProtocol()
                    + " / " + socket.getSession().getCipherSuite());

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);

            while (true) {
                System.out.print("> ");
                String msg = console.nextLine();
                out.println(msg);
                String reply = in.readLine();
                if (reply == null) break;
                System.out.println(reply);
                if (msg.trim().equalsIgnoreCase("QUIT")) break;
            }
        }
    }
}