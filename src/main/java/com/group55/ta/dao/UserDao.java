package com.group55.ta.dao;

import com.group55.ta.model.User;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for {@link User} entities.
 *
 * <p>All persistence is delegated to {@link FileStorageUtil}, which reads from
 * and writes to {@code data/users.txt}. Each non-blank, non-comment line in
 * that file is one CSV-encoded user record (see {@link User#toCSVLine()}).</p>
 *
 * <p><b>MVC Role:</b> DAO layer (between Model and Controller)</p>
 *
 * @author Group 55 - Dev-C
 * @version Sprint 1
 */
public class UserDao {

    /** Name of the backing data file relative to the {@code data/} directory. */
    private static final String USERS_FILE = "users.txt";

    /**
     * Persists a new {@link User} by appending its CSV representation to the
     * users data file.
     *
     * @param user the user to save; must not be {@code null}
     */
    public void save(User user) {
        if (user == null) {
            System.err.println("[UserDao] save() called with null user — skipped.");
            return;
        }
        FileStorageUtil.appendToFile(USERS_FILE, user.toCSVLine());
        System.out.println("[UserDao] Saved user: " + user.getUsername());
    }

    /**
     * Looks up a single {@link User} by their unique username.
     *
     * @param username the username to search for (case-sensitive)
     * @return the matching {@link User}, or {@code null} if not found
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        List<String> lines = FileStorageUtil.readLines(USERS_FILE);
        if (lines == null) {
            return null;
        }

        for (String line : lines) {
            User user = User.fromCSVLine(line);
            if (user != null && username.trim().equals(user.getUsername())) {
                return user;
            }
        }
        return null;
    }

    /**
     * Returns all users stored in the data file.
     *
     * <p>Blank lines and comment lines (starting with {@code #}) are
     * automatically skipped by {@link User#fromCSVLine(String)}.</p>
     *
     * @return a {@link List} of all valid {@link User} objects; never {@code null},
     *         but may be empty if the file is missing or contains no valid records
     */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        List<String> lines = FileStorageUtil.readLines(USERS_FILE);
        if (lines == null) {
            return users;
        }

        for (String line : lines) {
            User user = User.fromCSVLine(line);
            if (user != null) {
                users.add(user);
            }
        }
        return users;
    }

    /**
     * Validates a username/password pair against stored records.
     *
     * <p>Passwords are compared as plain text (no hashing in Sprint 1).</p>
     *
     * @param username the username to verify
     * @param password the plain-text password to verify
     * @return the authenticated {@link User} if credentials match,
     *         or {@code null} if the username does not exist or the password
     *         is incorrect
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        User user = findByUsername(username);
        if (user == null) {
            System.out.println("[UserDao] authenticate() — username not found: " + username);
            return null;
        }

        if (password.equals(user.getPassword())) {
            System.out.println("[UserDao] authenticate() — success for: " + username);
            return user;
        }

        System.out.println("[UserDao] authenticate() — wrong password for: " + username);
        return null;
    }
}
