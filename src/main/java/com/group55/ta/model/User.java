package com.group55.ta.model;

import com.group55.ta.util.DateTimeUtil;

/**
 * User account model (JSON per role folder under {@code data/users/}).
 */
public class User {
    private String userId;
    private String name;
    private String email;
    /** Step 3 will store real hash; Step 2 may still hold plaintext for migration. */
    private String passwordHash;
    private String role;
    private boolean active;
    private String createdAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Role getRoleEnum() {
        return Role.fromString(role);
    }

    public String getRoleLabel() {
        Role value = getRoleEnum();
        return value == null ? role : value.getLabel();
    }

    /** Short label for nav badges (Step 4 JSPs). */
    public String getNavBadge() {
        Role r = getRoleEnum();
        if (r == Role.ADMIN) {
            return "管理员";
        }
        if (r == Role.MO) {
            return "MO";
        }
        if (r == Role.TA) {
            return "TA";
        }
        return role != null ? role : "";
    }

    public String getDisplayCreatedAt() {
        return DateTimeUtil.formatDateTime(createdAt);
    }
}
