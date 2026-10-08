package com.secureproxy.server;

import com.secureproxy.models.User;
import com.secureproxy.security.LoginLimiter;
import com.secureproxy.security.PasswordHasher;
import com.secureproxy.storage.UserStore;

import java.util.Optional;

/**
 * Decides whether a login is accepted.
 *
 * Security choices:
 * - Unknown user, disabled user and wrong password all give the same answer
 *   (INVALID), so an attacker cannot learn which usernames exist.
 * - For unknown or disabled users a dummy bcrypt check is still done, so the
 *   response takes the same time as for a real user.
 * - Failures are counted per username AND per IP address. Counting is the same
 *   for names that do not exist, so lockouts do not reveal real accounts either.
 * - While locked, the password is not even checked.
 */
public class AuthService {

    public enum Status { OK, INVALID, LOCKED }

    public record Result(Status status, User user) {}

    private static final String DUMMY_HASH = PasswordHasher.hash("dummy-password-for-timing-only");
    private static final int MAX_USERNAME_CHARS = 64;
    private static final int MAX_PASSWORD_CHARS = 128;

    private final UserStore store;
    private final LoginLimiter byUser;
    private final LoginLimiter byIp;

    public AuthService(UserStore store, LoginLimiter byUser, LoginLimiter byIp) {
        this.store = store;
        this.byUser = byUser;
        this.byIp = byIp;
    }

    public Result login(String username, String password, String ip) {
        String ipKey = "ip:" + ip;

        if (username == null || password == null
                || username.length() > MAX_USERNAME_CHARS
                || password.length() > MAX_PASSWORD_CHARS) {
            byIp.recordFailure(ipKey);
            return new Result(Status.INVALID, null);
        }

        String userKey = "user:" + username;
        if (byIp.isLocked(ipKey) || byUser.isLocked(userKey)) {
            return new Result(Status.LOCKED, null);
        }

        Optional<User> found = store.find(username);
        boolean ok;
        if (found.isEmpty() || !found.get().enabled()) {
            PasswordHasher.verify(password, DUMMY_HASH); // keep timing the same
            ok = false;
        } else {
            ok = PasswordHasher.verify(password, found.get().passwordHash());
        }

        if (ok) {
            byUser.recordSuccess(userKey); // IP failures are deliberately NOT cleared
            return new Result(Status.OK, found.get());
        }

        byUser.recordFailure(userKey);
        byIp.recordFailure(ipKey);
        return new Result(Status.INVALID, null);
    }

    /** The user if they still exist and are enabled. Used to end sessions of disabled accounts. */
    public Optional<User> activeUser(String username) {
        return store.find(username).filter(User::enabled);
    }
}
