package com.rhythmcoders.tunnel.api;

import com.rhythmcoders.tunnel.proxy.SessionRegistry;
import com.rhythmcoders.tunnel.proxy.ClientSession;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;
import java.net.InetSocketAddress;
import java.io.OutputStream;

public class ManagementApi {
    
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    public static void startApiServer(int port) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // 1. Status Endpoint
        server.createContext("/api/status", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }
            
            int activeCount = SessionRegistry.getActiveSessions().size();
            String jsonResponse = "{\"status\":\"running\", \"activeTunnels\": " + activeCount + "}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(jsonResponse.getBytes()); }
        });

        // 2. Get Active Clients List
        server.createContext("/api/clients", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            StringBuilder json = new StringBuilder("[");
            var sessions = SessionRegistry.getActiveSessions();
            int i = 0;
            for (ClientSession s : sessions) {
                json.append("{\"clientId\":\"").append(s.getClientId())
                    .append("\", \"username\":\"").append(s.getUsername())
                    .append("\", \"connectedAt\":\"").append(s.getConnectedAt().toString()).append("\"}");
                if (++i < sessions.size()) json.append(",");
            }
            json.append("]");

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, json.toString().getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(json.toString().getBytes()); }
        });

        // 3. Disconnect / Remove Client
        server.createContext("/api/clients/disconnect", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                // Simple string parsing for clientId
                String clientId = body.replaceAll(".*\"clientId\"\\s*:\\s*\"([^\"]*)\".*", "$1");
                
                SessionRegistry.removeSession(clientId);
                System.out.println("[API] Admin revoked client session: " + clientId);

                String response = "{\"status\":\"revoked\"}";
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            }
        });

        // 4. Register New Client
        server.createContext("/api/auth/register", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) { exchange.sendResponseHeaders(204, -1); return; }

            try {
                String body = new String(exchange.getRequestBody().readAllBytes());
                String username = body.replaceAll(".*\"username\"\\s*:\\s*\"([^\"]*)\".*", "$1");
                String password = body.replaceAll(".*\"password\"\\s*:\\s*\"([^\"]*)\".*", "$1");

                String hashedPwd = BCrypt.hashpw(password, BCrypt.gensalt());
                boolean success = SessionRegistry.registerUser(username, hashedPwd);

                String response = "{\"success\":" + success + "}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(response.getBytes()); }
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Management API with Client Registry running on port: " + port);
    }
}