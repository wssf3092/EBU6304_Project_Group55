package com.group55.ta.dao;

import com.group55.ta.model.User;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link User} entities.
 * Persists user data to a CSV text file.
 *
 * <p>CSV line format: {@code username,password,email,fullName,role}</p>
 *
 * @author Group 55
 */
public class UserDao {

    private static final String DEFAULT_FILE = "data/users.txt";
    private final String filePath;

    public UserDao() {
        this.filePath = DEFAULT_FILE;
    }

    public UserDao(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Authenticate a user by username and password.
     *
     * @return the matching {@link User} if credentials are valid, otherwise {@code null}
     */
    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && user.getPassword() != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Find a user by their unique username.
     *
     * @return the {@link User} if found, otherwise {@code null}
     */
    public User findByUsername(String username) {
        if (username == null) return null;
        List<User> all = findAll();
        if (all == null) return null;
        for (User u : all) {
            if (username.equals(u.getUsername())) {
                return u;
            }
        }
        return null;
    }

    /**
     * Save (append) a new user. If the username already exists, returns {@code false}.
     *
     * @return {@code true} if saved successfully, {@code false} otherwise
     */
    public boolean save(User user) {
        if (user == null || user.getUsername() == null) return false;
        if (findByUsername(user.getUsername()) != null) return false;

        String line = toCsvLine(user);
        String existing = FileStorageUtil.readFile(filePath);
        if (existing != null && !existing.trim().isEmpty()) {
            return FileStorageUtil.appendToFile(filePath, "\n" + line);
        } else {
            return FileStorageUtil.writeFile(filePath, line);
        }
    }

    /**
     * Return all users from the data file.
     */
    public List<User> findAll() {
        List<String> lines = FileStorageUtil.readLines(filePath);
        if (lines == null) return new ArrayList<>();
        List<User> users = new ArrayList<>();
        for (String line : lines) {
            User u = fromCsvLine(line);
            if (u != null) {
                users.add(u);
            }
        }
        return users;
    }

    /**
     * Clear all data in the file (test utility).
     */
    public void clearAll() {
        FileStorageUtil.writeFile(filePath, "");
    }

    // ---- CSV helpers ----

    private String toCsvLine(User user) {
        return escape(user.getUsername()) + "," +
               escape(user.getPassword()) + "," +
               escape(user.getEmail()) + "," +
               escape(user.getFullName()) + "," +
               escape(user.getRole());
    }

    private User fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;
        // CSV order: username,password,email,fullName,role
        String username = parts[0].trim();
        String password = parts[1].trim();
        String email = parts[2].trim();
        String fullName = parts[3].trim();
        String role = parts[4].trim();
        // Constructor order: username, password, role, fullName, email
        return new User(username, password, role, fullName, email);
    }

    private static String escape(String s) {
        return s == null ? "" : s;
    }
}
