package com.rhythmcoders.tunnel;

import com.rhythmcoders.tunnel.api.ManagementApi;
import com.rhythmcoders.tunnel.proxy.ProxyServer;

public class TunnelApplication {
    public static void main(String[] args) {
        try {
            // 1. Start the HTTP Management API for Next.js control
            ManagementApi.startApiServer(8081);
            
            // 2. Start the Core TCP Proxy/Tunnel Engine
            new ProxyServer(8080).start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}