package backend;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class VpnClient {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== VPN Client Connection Setup ===");
        System.out.println("1. Local Wi-Fi (Local IPv4)");
        System.out.println("2. Remote Tunnel (ngrok)");
        System.out.print("Select connection mode (1 or 2): ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline character

        String serverHost;
        int port = 5000;

        if (choice == 1) {
            System.out.print("Enter local server IPv4 (e.g., 192.168.1.50): ");
            serverHost = scanner.nextLine();
        } else {
            System.out.print("Enter ngrok TCP host (e.g., 0.tcp.in.ngrok.io): ");
            serverHost = scanner.nextLine();
            System.out.print("Enter ngrok port number: ");
            port = scanner.nextInt();
            scanner.nextLine(); // consume newline
        }

        System.out.println("Connecting to VPN Server at " + serverHost + ":" + port + "...");

        try (
            Socket socket = new Socket(serverHost, port);
            InputStreamReader inputReader = new InputStreamReader(socket.getInputStream());
            BufferedReader reader = new BufferedReader(inputReader);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            System.out.println("Connected successfully! Type a message (type 'QUIT' to exit):");

            while (true) {
                System.out.print("> ");
                String message = scanner.nextLine();

                // Send message to server
                writer.println(message);

                if ("QUIT".equalsIgnoreCase(message.trim())) {
                    break;
                }

                // Read server response
                String response = reader.readLine();
                System.out.println("Server Response: " + response);
            }

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
        }
        
        scanner.close();
    }
}