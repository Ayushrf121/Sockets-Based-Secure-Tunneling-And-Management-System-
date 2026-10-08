package com.secureproxy.protocol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageTest {

    @Test
    void loginRoundTrip() {
        String json = Message.login("alice", "GoodPass123").toJson();
        Message back = Message.fromJson(json);

        assertNotNull(back);
        assertEquals(Message.LOGIN, back.type);
        assertEquals("alice", back.username);
        assertEquals("GoodPass123", back.password);
    }

    @Test
    void nullFieldsAreLeftOutOfJson() {
        assertEquals("{\"type\":\"PING\"}", Message.of(Message.PING).toJson());
    }

    @Test
    void invalidJsonReturnsNull() {
        assertNull(Message.fromJson("hello"));
        assertNull(Message.fromJson(""));
        assertNull(Message.fromJson("[]"));
        assertNull(Message.fromJson("{not json"));
        assertNull(Message.fromJson(null));
    }

    @Test
    void messageWithoutTypeReturnsNull() {
        assertNull(Message.fromJson("{\"username\":\"alice\"}"));
    }

    @Test
    void toStringNeverShowsPassword() {
        String text = Message.login("alice", "SuperSecret99").toString();
        assertFalse(text.contains("SuperSecret99"));
    }
}
