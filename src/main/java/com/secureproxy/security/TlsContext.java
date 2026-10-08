package com.secureproxy.security;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

/**
 * Builds TLS 1.3 socket factories from PKCS12 files.
 *
 * Server: uses its keystore (private key + certificate).
 * Client: trusts ONLY the certificates in its truststore (certificate pinning),
 *         so a different server or a man-in-the-middle is rejected at handshake.
 */
public final class TlsContext {

    public static final int DEFAULT_PORT = 5443;
    public static final String[] PROTOCOLS = {"TLSv1.3"};

    private TlsContext() {}

    private static KeyStore load(Path file, char[] password) throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (InputStream in = Files.newInputStream(file)) {
            ks.load(in, password);
        }
        return ks;
    }

    public static SSLServerSocketFactory serverFactory(Path keystore, char[] password) throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(load(keystore, password), password);
        SSLContext ctx = SSLContext.getInstance("TLSv1.3");
        ctx.init(kmf.getKeyManagers(), null, null);
        return ctx.getServerSocketFactory();
    }

    public static SSLSocketFactory clientFactory(Path truststore, char[] password) throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(load(truststore, password));
        SSLContext ctx = SSLContext.getInstance("TLSv1.3");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx.getSocketFactory();
    }

    /** Reads the keystore password from the PROXY_TLS_PASS environment variable. */
    public static char[] passwordFromEnv() {
        String p = System.getenv("PROXY_TLS_PASS");
        if (p == null || p.isEmpty()) {
            throw new IllegalStateException("Set the PROXY_TLS_PASS environment variable first");
        }
        return p.toCharArray();
    }
}