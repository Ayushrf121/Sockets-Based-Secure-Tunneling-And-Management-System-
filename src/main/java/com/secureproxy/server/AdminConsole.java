package com.secureproxy.server;

import com.secureproxy.models.User;
import com.secureproxy.security.PasswordHasher;
import com.secureproxy.security.PasswordPolicy;
import com.secureproxy.storage.UserStore;

import java.io.Console;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;

/**
 * Text console on the SERVER machine for managing users.
 * Only someone sitting at the server (or its terminal) can use it, so there is
 * no remote admin attack surface in this milestone. It also solves the
 * "first user" problem: the very first account is created here.
 */
public class AdminConsole implements Runnable {

    private final UserStore store;
    private final Runnable onExit;
    private final Scanner in = new Scanner(System.in);

    public AdminConsole(UserStore store, Runnable onExit) {
        this.store = store;
        this.onExit = onExit;
    }

    @Override
    public void run() {
        System.out.println("Admin console ready. Type 'help' for commands.");
        while (true) {
            System.out.print("admin> ");
            if (!in.hasNextLine()) break;
            String line = in.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] p = line.split("\\s+");
            try {
                if (!execute(p)) break;
            } catch (IOException | RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        onExit.run();
    }

    /** @return false when the console should stop */
    private boolean execute(String[] p) throws IOException {
        switch (p[0].toLowerCase()) {
            case "help" -> help();
            case "adduser" -> addUser(p);
            case "passwd" -> changePassword(p);
            case "listusers" -> listUsers();
            case "enable" -> setEnabled(p, true);
            case "disable" -> setEnabled(p, false);
            case "removeuser" -> removeUser(p);
            case "exit", "quit" -> {
                return false;
            }
            default -> System.out.println("Unknown command. Type 'help'.");
        }
        return true;
    }

    private void help() {
        System.out.println("""
                Commands:
                  adduser <name> [admin|user]   create a user (asks for a password)
                  passwd <name>                 change a user's password
                  listusers                     show all users
                  disable <name>                block login (ends their session on next message)
                  enable <name>                 allow login again
                  removeuser <name>             delete a user
                  exit                          stop the server""");
    }

    private void addUser(String[] p) throws IOException {
        if (p.length < 2 || p.length > 3) {
            System.out.println("Usage: adduser <name> [admin|user]");
            return;
        }
        String name = p[1];
        User.Role role = User.Role.USER;
        if (p.length == 3) {
            try {
                role = User.Role.valueOf(p[2].toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Role must be ADMIN or USER");
                return;
            }
        }
        if (store.find(name).isPresent()) {
            System.out.println("User '" + name + "' already exists.");
            return;
        }

        String password = readNewPassword(name);
        if (password == null) return;

        // add() rejects invalid usernames by throwing IllegalArgumentException (shown by run())
        boolean added = store.add(new User(name, PasswordHasher.hash(password), role, true));
        System.out.println(added ? "User created: " + name + " (" + role + ")" : "Could not create user.");
    }

    private void changePassword(String[] p) throws IOException {
        if (p.length != 2) {
            System.out.println("Usage: passwd <name>");
            return;
        }
        Optional<User> found = store.find(p[1]);
        if (found.isEmpty()) {
            System.out.println("No such user.");
            return;
        }
        String password = readNewPassword(p[1]);
        if (password == null) return;

        User u = found.get();
        store.update(new User(u.username(), PasswordHasher.hash(password), u.role(), u.enabled()));
        System.out.println("Password changed for " + u.username());
    }

    private void listUsers() {
        System.out.printf("%-20s %-6s %s%n", "USERNAME", "ROLE", "ENABLED");
        for (User u : store.list()) {
            System.out.printf("%-20s %-6s %s%n", u.username(), u.role(), u.enabled());
        }
    }

    private void setEnabled(String[] p, boolean enabled) throws IOException {
        if (p.length != 2) {
            System.out.println("Usage: " + p[0] + " <name>");
            return;
        }
        Optional<User> found = store.find(p[1]);
        if (found.isEmpty()) {
            System.out.println("No such user.");
            return;
        }
        store.update(found.get().withEnabled(enabled));
        System.out.println(p[1] + (enabled ? " enabled." : " disabled."));
    }

    private void removeUser(String[] p) throws IOException {
        if (p.length != 2) {
            System.out.println("Usage: removeuser <name>");
            return;
        }
        System.out.println(store.remove(p[1]) ? "Removed " + p[1] : "No such user.");
    }

    /** Asks twice, checks the password policy. Returns null if cancelled or invalid. */
    private String readNewPassword(String username) {
        String first = readPassword("New password: ");
        if (first == null) return null;
        String second = readPassword("Repeat password: ");
        if (!first.equals(second)) {
            System.out.println("Passwords do not match.");
            return null;
        }
        Optional<String> problem = PasswordPolicy.violation(username, first);
        if (problem.isPresent()) {
            System.out.println(problem.get());
            return null;
        }
        return first;
    }

    private String readPassword(String prompt) {
        Console c = System.console();
        if (c != null) {
            char[] pw = c.readPassword(prompt);
            return pw == null ? null : new String(pw);
        }
        System.out.print(prompt + "(typing is visible here) ");
        return in.hasNextLine() ? in.nextLine() : null;
    }
}
