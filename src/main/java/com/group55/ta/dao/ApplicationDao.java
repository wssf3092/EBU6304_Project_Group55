package com.group55.ta.dao;

import com.group55.ta.model.Application;
import com.group55.ta.model.Application.Status;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Application} entities.
 * Persists application data to a CSV text file.
 *
 * <p>CSV line format: {@code applicationId,studentUsername,courseId,status,applyTime,statement}</p>
 *
 * @author Group 55
 */
public class ApplicationDao {

    private static final String DEFAULT_FILE = "data/applications.txt";
    private final String filePath;

    public ApplicationDao() {
        this.filePath = DEFAULT_FILE;
    }

    public ApplicationDao(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Save (append) a new application.
     *
     * @return {@code true} if saved successfully, {@code false} otherwise
     */
    public boolean save(Application application) {
        if (application == null || application.getApplicationId() == null) return false;

        String line = toCsvLine(application);
        String existing = FileStorageUtil.readFile(filePath);
        if (existing != null && !existing.trim().isEmpty()) {
            return FileStorageUtil.appendToFile(filePath, "\n" + line);
        } else {
            return FileStorageUtil.writeFile(filePath, line);
        }
    }

    /**
     * Find all applications submitted by a specific student.
     */
    public List<Application> findByStudentUsername(String username) {
        List<Application> result = new ArrayList<>();
        if (username == null) return result;
        List<Application> all = findAll();
        for (Application a : all) {
            if (username.equals(a.getStudentUsername())) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Find all applications for a specific course.
     */
    public List<Application> findByCourseId(String courseId) {
        List<Application> result = new ArrayList<>();
        if (courseId == null) return result;
        List<Application> all = findAll();
        for (Application a : all) {
            if (courseId.equals(a.getCourseId())) {
                result.add(a);
            }
        }
        return result;
    }

    /**
     * Find a single application by its ID.
     *
     * @return the {@link Application} if found, otherwise {@code null}
     */
    public Application findById(String applicationId) {
        if (applicationId == null) return null;
        List<Application> all = findAll();
        for (Application a : all) {
            if (applicationId.equals(a.getApplicationId())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Update the status of an application by its ID.
     * Reads all lines, replaces the target line, and overwrites the file.
     */
    public boolean updateStatus(String applicationId, Status status) {
        if (applicationId == null || status == null) return false;
        List<String> lines = FileStorageUtil.readLines(filePath);
        if (lines == null || lines.isEmpty()) return false;

        boolean found = false;
        List<String> updatedLines = new ArrayList<>();
        for (String line : lines) {
            Application a = fromCsvLine(line);
            if (a != null && applicationId.equals(a.getApplicationId())) {
                a.setStatus(status);
                updatedLines.add(toCsvLine(a));
                found = true;
            } else {
                updatedLines.add(line);
            }
        }

        if (!found) return false;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < updatedLines.size(); i++) {
            if (i > 0) sb.append("\n");
            sb.append(updatedLines.get(i));
        }
        return FileStorageUtil.writeFile(filePath, sb.toString());
    }

    /**
     * Return all applications from the data file.
     */
    public List<Application> findAll() {
        List<String> lines = FileStorageUtil.readLines(filePath);
        if (lines == null) return new ArrayList<>();
        List<Application> apps = new ArrayList<>();
        for (String line : lines) {
            Application a = fromCsvLine(line);
            if (a != null) {
                apps.add(a);
            }
        }
        return apps;
    }

    /**
     * Clear all data in the file (test utility).
     */
    public void clearAll() {
        FileStorageUtil.writeFile(filePath, "");
    }

    // ---- CSV helpers ----

    private String toCsvLine(Application app) {
        return escape(app.getApplicationId()) + "," +
               escape(app.getStudentUsername()) + "," +
               escape(app.getCourseId()) + "," +
               (app.getStatus() == null ? "PENDING" : app.getStatus().name()) + "," +
               escape(app.getApplyTime()) + "," +
               escape(app.getStatement());
    }

    private Application fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",", -1);
        if (parts.length < 6) return null;
        String applicationId = parts[0].trim();
        String studentUsername = parts[1].trim();
        String courseId = parts[2].trim();
        Status status;
        try {
            status = Status.valueOf(parts[3].trim());
        } catch (IllegalArgumentException e) {
            status = Status.PENDING;
        }
        String applyTime = parts[4].trim();
        String statement = parts[5].trim();
        return new Application(applicationId, studentUsername, courseId, status, applyTime, statement);
    }

    private static String escape(String s) {
        return s == null ? "" : s;
    }
}
