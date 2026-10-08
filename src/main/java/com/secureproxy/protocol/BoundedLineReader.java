package com.secureproxy.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Reads lines like BufferedReader.readLine(), but refuses lines longer than a
 * limit. A plain readLine() keeps reading forever if a client never sends a
 * newline, which lets one client use up all the server's memory.
 */
public final class BoundedLineReader {

    private final BufferedReader in;
    private final int maxChars;

    public BoundedLineReader(Reader reader, int maxChars) {
        this.in = (reader instanceof BufferedReader br) ? br : new BufferedReader(reader);
        this.maxChars = maxChars;
    }

    /**
     * @return the next line without its line ending, or null at end of stream
     * @throws IOException if the line is longer than the limit (the caller should disconnect)
     */
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\n') {
                int len = sb.length();
                if (len > 0 && sb.charAt(len - 1) == '\r') sb.setLength(len - 1);
                return sb.toString();
            }
            if (sb.length() >= maxChars) {
                throw new IOException("Line too long (limit " + maxChars + " characters)");
            }
            sb.append((char) c);
        }
        return sb.length() == 0 ? null : sb.toString();
    }
}
