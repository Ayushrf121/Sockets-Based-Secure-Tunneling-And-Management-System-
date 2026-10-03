import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VpnServer {
    private static final int PORT = 5000;
    // Thread pool to handle up to 10 concurrent clients simultaneously
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("Starting VPN Server on port " + PORT + "...");
        // Server sockets waits for the request to come in over the network.
        // It performs some operation based on that request, and then possibly returns a result to the requester.
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Wait for a client connection (blocking call)
                // This class implements client sockets (also called just "sockets"). A socket is an endpoint for communication between two machines.
                //accept():- Listens for a connection to be made to this socket and accepts it. 
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getRemoteSocketAddress());

                // Hand off the client connection to a background worker thread
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }
    // The Runnable interface should be implemented by any class whose instances are intended to be executed by a thread.
    private static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (
                InputStreamReader inputReader = new InputStreamReader(socket.getInputStream());
                BufferedReader reader = new BufferedReader(inputReader);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
            ) {
                String inputLine;
                // Read messages sent by the client
                while ((inputLine = reader.readLine()) != null) {
                    System.out.println("Received from [" + socket.getRemoteSocketAddress() + "]: " + inputLine);
                    
                    // Echo the message back
                    writer.println("ECHO: " + inputLine);

                    if ("QUIT".equalsIgnoreCase(inputLine.trim())) {
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected abruptly: " + socket.getRemoteSocketAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}