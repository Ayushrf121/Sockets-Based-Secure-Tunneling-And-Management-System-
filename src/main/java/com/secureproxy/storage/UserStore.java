package com.secureproxy.storage;

import com.secureproxy.models.User;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Abstraction over where users live. Phase 1: file. Phase 2 or 3: database.
 */
public interface UserStore {
    Optional<User> find(String username);

    /** @return false if the username already exists */
    boolean add(User user) throws IOException;

    boolean remove(String username) throws IOException;

    /** Replaces an existing user (e.g. to enable/disable). */
    boolean update(User user) throws IOException;

    List<User> list();
}