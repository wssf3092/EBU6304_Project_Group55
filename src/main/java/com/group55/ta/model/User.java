package com.group55.ta.model;

/**
 * Represents a user in the TA Recruitment System.
 *
 * <p>A user can be a Student, Teacher, or Admin, determined by the {@link Role} enum.
 * This class supports CSV-based serialization for file storage via
 * {@link #toCSVLine()} and {@link #fromCSVLine(String)}.</p>
 *
 * <p><b>MVC Role:</b> Model (M layer)</p>
 *
 * @author Group 55 - Dev-B
 * @version Sprint 1
 */
public class User {

    /**
     * Defines the roles a user can have in the system.
     */
    public enum Role {
        STUDENT,
        TEACHER,
        ADMIN
    }

    private String username;
    private String password;
    private String email;
    private String fullName;
    private Role role;

    // ─── Constructors ───────────────────────────────────────────────────────────

    /**
     * Default no-arg constructor.
     */
    public User() {
    }

    /**
     * Full-argument constructor.
     *
     * @param username the unique login name of the user
     * @param password the plain-text password (will be stored as-is for this sprint)
     * @param email    the user's email address
     * @param fullName the user's full display name
     * @param role     the user's role in the system
     */
    public User(String username, String password, String email, String fullName, Role role) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // ─── CSV Serialization ──────────────────────────────────────────────────────

    /**
     * Serializes this User to a CSV line for file storage.
     *
     * <p>Format: {@code username,password,email,fullName,role}</p>
     *
     * <p><b>Note:</b> Field values must not contain commas. If a field is
     * {@code null}, an empty string is written in its place.</p>
     *
     * @return a comma-separated string representing this user
     */
    public String toCSVLine() {
        return (username  != null ? username  : "") + ","
             + (password  != null ? password  : "") + ","
             + (email     != null ? email     : "") + ","
             + (fullName  != null ? fullName  : "") + ","
             + (role      != null ? role.name() : "");
    }

    /**
     * Deserializes a CSV line back into a {@link User} object.
     *
     * <p>Format: {@code username,password,email,fullName,role}</p>
     *
     * @param csvLine a comma-separated string in the format produced by {@link #toCSVLine()}
     * @return the reconstructed {@link User}, or {@code null} if the line is
     *         blank, a comment (starts with {@code #}), or malformed
     */
    public static User fromCSVLine(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty() || csvLine.trim().startsWith("#")) {
            return null;
        }

        String[] parts = csvLine.trim().split(",", 5);
        if (parts.length < 5) {
            return null;
        }

        User user = new User();
        user.setUsername(parts[0].trim());
        user.setPassword(parts[1].trim());
        user.setEmail(parts[2].trim());
        user.setFullName(parts[3].trim());

        try {
            user.setRole(Role.valueOf(parts[4].trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Unknown role value — skip this record
            return null;
        }

        return user;
    }

    // ─── Object overrides ───────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "User{"
                + "username='" + username + '\''
                + ", email='" + email + '\''
                + ", fullName='" + fullName + '\''
                + ", role=" + role
                + '}';
    }
}
