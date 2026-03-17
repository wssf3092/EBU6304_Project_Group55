package com.group55.ta.dao;

import com.group55.ta.model.Application;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for {@link Application} entities.
 *
 * <p>All persistence is delegated to {@link FileStorageUtil}, which reads from
 * and writes to {@code data/applications.txt}. Each non-blank, non-comment line
 * in that file is one CSV-encoded application record (see
 * {@link Application#toCSVLine()}).</p>
 *
 * <p>The {@link #updateStatus(String, Application.Status)} method uses a
 * read-modify-write strategy: it loads all records into memory, updates the
 * target record, then overwrites the entire file. Comment header lines at the
 * top of the file are preserved during this operation.</p>
 *
 * <p><b>MVC Role:</b> DAO layer (between Model and Controller)</p>
 *
 * @author Group 55 - Dev-C
 * @version Sprint 1
 */
public class ApplicationDao {

    /** Name of the backing data file relative to the {@code data/} directory. */
    private static final String APPLICATIONS_FILE = "applications.txt";

    /**
     * Persists a new {@link Application} by appending its CSV representation
     * to the applications data file.
     *
     * @param app the application to save; must not be {@code null}
     */
    public void save(Application app) {
        if (app == null) {
            System.err.println("[ApplicationDao] save() called with null application — skipped.");
            return;
        }
        FileStorageUtil.appendToFile(APPLICATIONS_FILE, app.toCSVLine());
        System.out.println("[ApplicationDao] Saved application: " + app.getApplicationId()
                + " by " + app.getStudentUsername());
    }

    /**
     * Returns all applications submitted by a specific student.
     *
     * @param username the student's username to filter by (case-sensitive)
     * @return a {@link List} of matching {@link Application} objects; never
     *         {@code null}, but may be empty
     */
    public List<Application> findByStudentUsername(String username) {
        List<Application> result = new ArrayList<>();
        if (username == null || username.trim().isEmpty()) {
            return result;
        }

        List<String> lines = FileStorageUtil.readLines(APPLICATIONS_FILE);
        if (lines == null) {
            return result;
        }

        for (String line : lines) {
            Application app = Application.fromCSVLine(line);
            if (app != null && username.trim().equals(app.getStudentUsername())) {
                result.add(app);
            }
        }
        return result;
    }

    /**
     * Returns all applications submitted for a specific course.
     *
     * @param courseId the course ID to filter by (case-sensitive)
     * @return a {@link List} of matching {@link Application} objects; never
     *         {@code null}, but may be empty
     */
    public List<Application> findByCourseId(String courseId) {
        List<Application> result = new ArrayList<>();
        if (courseId == null || courseId.trim().isEmpty()) {
            return result;
        }

        List<String> lines = FileStorageUtil.readLines(APPLICATIONS_FILE);
        if (lines == null) {
            return result;
        }

        for (String line : lines) {
            Application app = Application.fromCSVLine(line);
            if (app != null && courseId.trim().equals(app.getCourseId())) {
                result.add(app);
            }
        }
        return result;
    }

    /**
     * Returns all applications stored in the data file.
     *
     * <p>Blank lines and comment lines (starting with {@code #}) are
     * automatically skipped by {@link Application#fromCSVLine(String)}.</p>
     *
     * @return a {@link List} of all valid {@link Application} objects; never
     *         {@code null}, but may be empty if the file is missing or empty
     */
    public List<Application> findAll() {
        List<Application> applications = new ArrayList<>();
        List<String> lines = FileStorageUtil.readLines(APPLICATIONS_FILE);
        if (lines == null) {
            return applications;
        }

        for (String line : lines) {
            Application app = Application.fromCSVLine(line);
            if (app != null) {
                applications.add(app);
            }
        }
        return applications;
    }

    /**
     * Updates the status of an existing application identified by its ID.
     *
     * <p>Strategy: read all raw lines from the file → find and replace the
     * matching record's status → overwrite the entire file with the updated
     * content. Comment header lines (starting with {@code #}) and blank lines
     * are preserved as-is.</p>
     *
     * @param applicationId the ID of the application to update
     * @param newStatus     the new {@link Application.Status} to set
     * @return {@code true} if the record was found and updated successfully;
     *         {@code false} if no matching record was found or an error occurred
     */
    public boolean updateStatus(String applicationId, Application.Status newStatus) {
        if (applicationId == null || applicationId.trim().isEmpty() || newStatus == null) {
            System.err.println("[ApplicationDao] updateStatus() called with invalid arguments — skipped.");
            return false;
        }

        List<String> lines = FileStorageUtil.readLines(APPLICATIONS_FILE);
        if (lines == null) {
            System.err.println("[ApplicationDao] updateStatus() — could not read " + APPLICATIONS_FILE);
            return false;
        }

        boolean updated = false;
        StringBuilder newContent = new StringBuilder();

        for (String line : lines) {
            Application app = Application.fromCSVLine(line);
            if (app != null && applicationId.trim().equals(app.getApplicationId())) {
                // Replace this record with the updated status
                app.setStatus(newStatus);
                newContent.append(app.toCSVLine()).append(System.lineSeparator());
                updated = true;
            } else {
                // Preserve all other lines (including comments and blanks) unchanged
                newContent.append(line).append(System.lineSeparator());
            }
        }

        if (!updated) {
            System.out.println("[ApplicationDao] updateStatus() — applicationId not found: " + applicationId);
            return false;
        }

        FileStorageUtil.writeFile(APPLICATIONS_FILE, newContent.toString());
        System.out.println("[ApplicationDao] Updated status of application " + applicationId
                + " to " + newStatus.name());
        return true;
    }
}
