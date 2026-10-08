package com.secureproxy.server;

import com.secureproxy.models.User;
import com.secureproxy.security.LoginLimiter;
import com.secureproxy.security.PasswordHasher;
import com.secureproxy.server.AuthService.Status;
import com.secureproxy.storage.FileUserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private static final String IP = "192.168.1.10";

    @TempDir
    Path dir;

    private FileUserStore store;
    private AuthService auth;

    @BeforeEach
    void setUp() throws Exception {
        store = new FileUserStore(dir.resolve("users.db"));
        store.add(new User("alice", PasswordHasher.hash("GoodPass123"), User.Role.USER, true));
        store.add(new User("bob", PasswordHasher.hash("BobPass1234"), User.Role.USER, false));
        auth = new AuthService(store,
                new LoginLimiter(3, Duration.ofMinutes(15)),
                new LoginLimiter(100, Duration.ofMinutes(15)));
    }

    @Test
    void correctPasswordLogsIn() {
        AuthService.Result r = auth.login("alice", "GoodPass123", IP);
        assertEquals(Status.OK, r.status());
        assertEquals("alice", r.user().username());
    }

    @Test
    void wrongPasswordIsInvalid() {
        AuthService.Result r = auth.login("alice", "WrongPass999", IP);
        assertEquals(Status.INVALID, r.status());
        assertNull(r.user());
    }

    @Test
    void unknownUserIsInvalid() {
        assertEquals(Status.INVALID, auth.login("ghost", "GoodPass123", IP).status());
    }

    @Test
    void disabledUserIsInvalidEvenWithRightPassword() {
        assertEquals(Status.INVALID, auth.login("bob", "BobPass1234", IP).status());
    }

    @Test
    void lockedAfterRepeatedFailuresEvenForCorrectPassword() {
        for (int i = 0; i < 3; i++) {
            assertEquals(Status.INVALID, auth.login("alice", "WrongPass999", IP).status());
        }
        assertEquals(Status.LOCKED, auth.login("alice", "GoodPass123", IP).status());
    }

    @Test
    void unknownUsersLockToo_soLockoutDoesNotRevealAccounts() {
        for (int i = 0; i < 3; i++) {
            assertEquals(Status.INVALID, auth.login("ghost", "whatever123", IP).status());
        }
        assertEquals(Status.LOCKED, auth.login("ghost", "whatever123", IP).status());
    }

    @Test
    void successfulLoginResetsFailureCount() {
        auth.login("alice", "WrongPass999", IP);
        auth.login("alice", "WrongPass999", IP);
        assertEquals(Status.OK, auth.login("alice", "GoodPass123", IP).status());

        auth.login("alice", "WrongPass999", IP);
        auth.login("alice", "WrongPass999", IP);
        assertEquals(Status.OK, auth.login("alice", "GoodPass123", IP).status());
    }

    @Test
    void ipAddressLocksAfterTooManyFailuresAcrossUsernames() {
        AuthService strictIp = new AuthService(store,
                new LoginLimiter(100, Duration.ofMinutes(15)),
                new LoginLimiter(2, Duration.ofMinutes(15)));

        strictIp.login("x1", "whatever123", IP);
        strictIp.login("x2", "whatever123", IP);

        assertEquals(Status.LOCKED, strictIp.login("alice", "GoodPass123", IP).status());
        assertEquals(Status.OK, strictIp.login("alice", "GoodPass123", "192.168.1.99").status());
    }

    @Test
    void nullAndOversizedInputAreInvalid() {
        assertEquals(Status.INVALID, auth.login(null, "GoodPass123", IP).status());
        assertEquals(Status.INVALID, auth.login("alice", null, IP).status());
        assertEquals(Status.INVALID, auth.login("a".repeat(65), "GoodPass123", IP).status());
        assertEquals(Status.INVALID, auth.login("alice", "p".repeat(129), IP).status());
    }

    @Test
    void activeUserReflectsDisabling() throws Exception {
        assertTrue(auth.activeUser("alice").isPresent());
        store.update(store.find("alice").get().withEnabled(false));
        assertTrue(auth.activeUser("alice").isEmpty());
        assertTrue(auth.activeUser("ghost").isEmpty());
    }
}
