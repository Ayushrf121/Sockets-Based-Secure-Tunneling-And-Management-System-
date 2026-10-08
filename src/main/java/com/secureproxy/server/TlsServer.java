package com.secureproxy.server;

import com.secureproxy.security.LoginLimiter;
import com.secureproxy.security.NetworkGuard;
import com.secureproxy.security.TlsContext;
import com.secureproxy.storage.FileUserStore;
import com.secureproxy.storage.UserStore;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Milestone 3 server: TLS 1.3 + login.
 *
 * - The accept loop runs in a background thread.
 * - The main thread runs the admin console (type 'exit' there to stop the server).
 * - Users are saved in users.db in the folder the server is started from.
 */
public class TlsServer {

    private static final Path USERS_FILE = Path.of("users.db");
    private static final int MAX_WORKERS = 20;
    private static final int MAX_WAITING = 10;

    public static void main(String[] args) throws Exception {
        UserStore store = new FileUserStore(USERS_FILE);
        AuthService auth = new AuthService(store,
                new LoginLimiter(5, Duration.ofMinutes(15)),    // per username
                new LoginLimiter(10, Duration.ofMinutes(15)));  // per IP address

        SSLServerSocketFactory factory =
                TlsContext.serverFactory(Path.of("certs/server.p12"), TlsContext.passwordFromEnv());

        // 20 workers + 10 waiting. When full, new connections are refused instead of piling up.
        ThreadPoolExecutor pool = new ThreadPoolExecutor(
                MAX_WORKERS, MAX_WORKERS, 0L, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(MAX_WAITING), daemonThreads());

        SSLServerSocket server = (SSLServerSocket) factory.createServerSocket(TlsContext.DEFAULT_PORT);
        server.setEnabledProtocols(TlsContext.PROTOCOLS);
        System.out.println("[SERVER] TLS 1.3 listening on port " + TlsContext.DEFAULT_PORT);
        System.out.println("[SERVER] Users file: " + USERS_FILE.toAbsolutePath());

        Thread acceptor = new Thread(() -> acceptLoop(server, pool, auth), "acceptor");
        acceptor.setDaemon(true);
        acceptor.start();

        new AdminConsole(store, () -> shutdown(server, pool)).run();
    }

    private static void acceptLoop(SSLServerSocket server, ExecutorService pool, AuthService auth) {
        while (!server.isClosed()) {
            try {
                Socket client = server.accept();

                if (!NetworkGuard.isAllowed(client.getInetAddress())) {
                    System.out.println("[SERVER] Rejected non-local address: " + client.getRemoteSocketAddress());
                    client.close();
                    continue;
                }
                try {
                    pool.execute(new ClientHandler(client, auth));
                } catch (RejectedExecutionException busy) {
                    System.out.println("[SERVER] Busy, refused " + client.getRemoteSocketAddress());
                    client.close();
                }
            } catch (IOException e) {
                if (server.isClosed()) break;
                System.out.println("[SERVER] Accept error: " + e.getMessage());
            }
        }
    }

    private static void shutdown(SSLServerSocket server, ExecutorService pool) {
        System.out.println("[SERVER] Shutting down...");
        try {
            server.close();
        } catch (IOException ignored) {
            // closing anyway
        }
        pool.shutdownNow();
    }

    /** Daemon threads, so the program can exit even if a client is still connected. */
    private static ThreadFactory daemonThreads() {
        return r -> {
            Thread t = new Thread(r, "client-worker");
            t.setDaemon(true);
            return t;
        };
    }
}
