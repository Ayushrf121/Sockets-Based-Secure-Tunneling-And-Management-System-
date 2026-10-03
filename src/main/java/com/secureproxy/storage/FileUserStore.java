package com.secureproxy.storage;

import com.secureproxy.models.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Stores users in a text file: username|bcrypt_hash|ROLE|enabled
 * Passwords are never stored in plain text, only bcrypt hashes.
 */
public class FileUserStore implements UserStore {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,32}");

    private final Path file;
    private final Map<String, User> users = new LinkedHashMap<>();

    public FileUserStore(Path file) throws IOException {
        this.file = file;
        load();
    }

    private void load() throws IOException {
        if (!Files.exists(file)) return;
        for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            String[] p = line.split("\\|");
            if (p.length != 4) continue;
            users.put(p[0], new User(p[0], p[1], User.Role.valueOf(p[2]), Boolean.parseBoolean(p[3])));
        }
    }

    private void save() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# username|bcrypt_hash|ROLE|enabled");
        for (User u : users.values()) {
            lines.add(u.username() + "|" + u.passwordHash() + "|" + u.role() + "|" + u.enabled());
        }
        // write to a temp file first so a crash can't corrupt the real one
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(tmp, lines, StandardCharsets.UTF_8);
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public synchronized Optional<User> find(String username) {
        return Optional.ofNullable(users.get(username));
    }

    @Override
    public synchronized boolean add(User user) throws IOException {
        if (!USERNAME.matcher(user.username()).matches()) {
            throw new IllegalArgumentException("Username must be 3-32 chars: letters, digits, underscore");
        }
        if (users.containsKey(user.username())) return false;
        users.put(user.username(), user);
        save();
        return true;
    }

    @Override
    public synchronized boolean remove(String username) throws IOException {
        if (users.remove(username) == null) return false;
        save();
        return true;
    }

    @Override
    public synchronized boolean update(User user) throws IOException {
        if (!users.containsKey(user.username())) return false;
        users.put(user.username(), user);
        save();
        return true;
    }

    @Override
    public synchronized List<User> list() {
        return new ArrayList<>(users.values());
    }
}