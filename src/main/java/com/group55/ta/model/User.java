package com.group55.ta.model;

import java.io.Serializable;

/**
 * User model representing a system user (Student, Teacher, or Admin).
 *
 * <p>CSV format in {@code data/users.txt}:
 * {@code username,password,email,fullName,role}</p>
 *
 * @author Group 55
 */
public class User implements Serializable {

    private String username;
    private String password;
    private String role;
    private String fullName;
    private String email;

    /** No-arg constructor. */
    public User() {
    }

    /**
     * Full constructor matching test expectations.
     *
     * @param username the unique login name
     * @param password the plain-text password
     * @param role     the role string, e.g. "Student", "Teacher", "Admin"
     * @param fullName the user's display name
     * @param email    the user's email address
     */
    public User(String username, String password, String role, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
    }

    // ---- Getters & Setters ----

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Alias for {@link #getFullName()} to support JSP EL expressions like
     * {@code ${user.name}}.
     */
    public String getName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User{username='" + username + "', role='" + role + "', fullName='" + fullName + "'}";
    }
}
