package com.group55.ta.util;

import com.group55.ta.model.Role;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Centralized data directory paths (supports {@code -Dta.data.root}).
 */
public final class AppPaths {
    private static final Object LOCK = new Object();
    private static Path dataRoot;

    private AppPaths() {
    }

    public static Path getDataRoot() {
        synchronized (LOCK) {
            if (dataRoot == null) {
                String configured = System.getProperty("ta.data.root");
                if (configured != null && !configured.trim().isEmpty()) {
                    dataRoot = Paths.get(configured).toAbsolutePath().normalize();
                } else {
                    dataRoot = Paths.get(System.getProperty("user.dir"), "data").toAbsolutePath().normalize();
                }
                ensureStructure();
            }
            return dataRoot;
        }
    }

    public static void overrideDataRoot(Path root) {
        synchronized (LOCK) {
            dataRoot = root.toAbsolutePath().normalize();
            ensureStructure();
        }
    }

    public static Path users(Role role) {
        return getDataRoot().resolve("users").resolve(role.getFolder());
    }

    public static Path courses() {
        return getDataRoot().resolve("courses");
    }

    public static Path applications() {
        return getDataRoot().resolve("applications");
    }

    private static void ensureStructure() {
        try {
            Files.createDirectories(getDataRoot().resolve("users").resolve("tas"));
            Files.createDirectories(getDataRoot().resolve("users").resolve("mos"));
            Files.createDirectories(getDataRoot().resolve("users").resolve("admins"));
            Files.createDirectories(courses());
            Files.createDirectories(applications());
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create data directories", ex);
        }
    }
}
