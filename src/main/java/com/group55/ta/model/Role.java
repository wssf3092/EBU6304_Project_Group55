package com.group55.ta.model;

/**
 * User roles supported by the system.
 */
public enum Role {
    TA("tas", "TA", "TA Workspace", "/ta/dashboard"),
    MO("mos", "MO", "MO Workspace", "/mo/dashboard"),
    ADMIN("admins", "ADM", "Admin Workspace", "/admin/workload");

    private final String folder;
    private final String idPrefix;
    private final String label;
    private final String homePath;

    Role(String folder, String idPrefix, String label, String homePath) {
        this.folder = folder;
        this.idPrefix = idPrefix;
        this.label = label;
        this.homePath = homePath;
    }

    public String getFolder() {
        return folder;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public String getLabel() {
        return label;
    }

    public String getHomePath() {
        return homePath;
    }

    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(value)) {
                return role;
            }
        }
        return null;
    }
}
