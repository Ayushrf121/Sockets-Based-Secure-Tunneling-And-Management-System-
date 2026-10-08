package com.secureproxy.protocol;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class BoundedLineReaderTest {

    private static BoundedLineReader reader(String text, int max) {
        return new BoundedLineReader(new StringReader(text), max);
    }

    @Test
    void readsSeveralLines() throws IOException {
        BoundedLineReader r = reader("one\ntwo\n", 100);
        assertEquals("one", r.readLine());
        assertEquals("two", r.readLine());
        assertNull(r.readLine());
    }

    @Test
    void stripsCarriageReturn() throws IOException {
        assertEquals("hello", reader("hello\r\n", 100).readLine());
    }

    @Test
    void returnsNullWhenStreamIsEmpty() throws IOException {
        assertNull(reader("", 100).readLine());
    }

    @Test
    void returnsLastLineWithoutNewline() throws IOException {
        assertEquals("tail", reader("tail", 100).readLine());
    }

    @Test
    void lineOfExactlyMaxLengthIsAllowed() throws IOException {
        assertEquals("abcde", reader("abcde\n", 5).readLine());
    }

    @Test
    void tooLongLineThrows() {
        assertThrows(IOException.class, () -> reader("abcdef\n", 5).readLine());
    }
}
