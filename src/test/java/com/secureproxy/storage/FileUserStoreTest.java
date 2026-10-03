package com.secureproxy.storage;

import com.secureproxy.models.User;
import com.secureproxy.security.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileUserStoreTest {

    @TempDir
    Path dir;

    private User newUser(String name) {
        return new User(name, PasswordHasher.hash("pw-" + name), User.Role.USER, true);
    }

    @Test
    void addThenFind() throws Exception {
        FileUserStore store = new FileUserStore(dir.resolve("users.db"));
        assertTrue(store.add(newUser("alice")));
        assertTrue(store.find("alice").isPresent());
    }

    @Test
    void duplicateUsernameRejected() throws Exception {
        FileUserStore store = new FileUserStore(dir.resolve("users.db"));
        assertTrue(store.add(newUser("alice")));
        assertFalse(store.add(newUser("alice")));
    }

    @Test
    void dataSurvivesReload() throws Exception {
        Path file = dir.resolve("users.db");
        new FileUserStore(file).add(newUser("bob"));
        FileUserStore reloaded = new FileUserStore(file);
        assertTrue(reloaded.find("bob").isPresent());
    }

    @Test
    void fileNeverContainsPlainPassword() throws Exception {
        Path file = dir.resolve("users.db");
        new FileUserStore(file).add(newUser("carol"));
        assertFalse(Files.readString(file).contains("pw-carol"));
    }

    @Test
    void invalidUsernameRejected() throws Exception {
        FileUserStore store = new FileUserStore(dir.resolve("users.db"));
        assertThrows(IllegalArgumentException.class, () -> store.add(newUser("a|b")));
    }

    @Test
    void removeAndDisable() throws Exception {
        FileUserStore store = new FileUserStore(dir.resolve("users.db"));
        store.add(newUser("dave"));
        store.update(store.find("dave").get().withEnabled(false));
        assertFalse(store.find("dave").get().enabled());
        assertTrue(store.remove("dave"));
        assertTrue(store.find("dave").isEmpty());
    }
}