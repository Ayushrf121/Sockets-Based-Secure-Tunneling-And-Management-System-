package com.rhythmcoders.tunnel.api;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
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
        
        // Status Endpoint
        server.createContext("/api/status", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            String jsonResponse = "{\"status\":\"running\", \"activeTunnels\": 1}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(jsonResponse.getBytes());
            }
        });

        // Start Tunnel Endpoint
        server.createContext("/api/start", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                // Read incoming body bytes so the stream is fully consumed
                if (exchange.getRequestBody() != null) {
                    exchange.getRequestBody().readAllBytes();
                }
                System.out.println("[API] Command received: START tunnel.");
                
                String response = "{\"status\":\"success\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });

        // Stop Tunnel Endpoint
        server.createContext("/api/stop", exchange -> {
            setCorsHeaders(exchange);
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            try {
                System.out.println("[API] Command received: STOP tunnels.");
                
                String response = "{\"status\":\"stopped\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
                exchange.sendResponseHeaders(500, -1);
            }
        });

        server.setExecutor(null);
        server.start();
        System.out.println("Next.js Management API running on port: " + port);
    }
}