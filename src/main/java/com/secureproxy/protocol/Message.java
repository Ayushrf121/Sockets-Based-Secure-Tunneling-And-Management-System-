package com.secureproxy.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * One protocol message. Sent as a single line of JSON, for example:
 *   {"type":"LOGIN","username":"alice","password":"..."}
 *
 * Fields that are null are left out of the JSON, so each message type only
 * carries the fields it needs.
 */
public final class Message {

    // Message types
    public static final String LOGIN = "LOGIN";
    public static final String LOGIN_OK = "LOGIN_OK";
    public static final String ERROR = "ERROR";
    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String WHOAMI = "WHOAMI";
    public static final String ECHO = "ECHO";
    public static final String LOGOUT = "LOGOUT";
    public static final String BYE = "BYE";

    private static final Gson GSON = new Gson();

    public String type;
    public String username;
    public String password;
    public String role;
    public String text;
    public String code;
    public String message;

    public static Message of(String type) {
        Message m = new Message();
        m.type = type;
        return m;
    }

    public static Message login(String username, String password) {
        Message m = of(LOGIN);
        m.username = username;
        m.password = password;
        return m;
    }

    public static Message error(String code, String message) {
        Message m = of(ERROR);
        m.code = code;
        m.message = message;
        return m;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    /** @return the parsed message, or null if the text is not valid JSON or has no "type". */
    public static Message fromJson(String json) {
        if (json == null) return null;
        try {
            Message m = GSON.fromJson(json, Message.class);
            if (m == null || m.type == null) return null;
            return m;
        } catch (JsonParseException e) {
            return null;
        }
    }

    /** Never prints the password, so accidental logging cannot leak it. */
    @Override
    public String toString() {
        return "Message[type=" + type + "]";
    }
}
