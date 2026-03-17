package com.group55.ta.model;

/**
 * Represents a TA application submitted by a student for a specific course.
 *
 * <p>Each application carries the student's personal statement and a review
 * status managed by the course teacher or admin.
 * This class supports CSV-based serialization for file storage via
 * {@link #toCSVLine()} and {@link #fromCSVLine(String)}.</p>
 *
 * <p>CSV format (6 fields, statement placed last to tolerate embedded commas):</p>
 * <pre>applicationId,studentUsername,courseId,status,applyTime,statement</pre>
 *
 * <p><b>MVC Role:</b> Model (M layer)</p>
 *
 * @author Group 55 - Dev-B
 * @version Sprint 1
 */
public class Application {

    /**
     * Lifecycle status of a TA application.
     */
    public enum Status {
        /** Submitted but not yet reviewed. */
        PENDING,
        /** Approved by the course teacher / admin. */
        APPROVED,
        /** Rejected by the course teacher / admin. */
        REJECTED
    }

    private String applicationId;
    private String studentUsername;
    private String courseId;
    private String statement;
    private Status status;
    /** Submission timestamp stored as a formatted string, e.g. {@code "2025-03-17 10:30:00"}. */
    private String applyTime;

    // ─── Constructors ───────────────────────────────────────────────────────────

    /**
     * Default no-arg constructor.
     */
    public Application() {
    }

    /**
     * Full-argument constructor.
     *
     * @param applicationId   the unique identifier of this application (e.g. UUID or sequential ID)
     * @param studentUsername the username of the applying student
     * @param courseId        the ID of the course the student is applying to TA for
     * @param statement       the student's personal statement / motivation letter
     * @param status          the current review status of the application
     * @param applyTime       the submission timestamp as a formatted string
     */
    public Application(String applicationId, String studentUsername, String courseId,
                       String statement, Status status, String applyTime) {
        this.applicationId   = applicationId;
        this.studentUsername = studentUsername;
        this.courseId        = courseId;
        this.statement       = statement;
        this.status          = status;
        this.applyTime       = applyTime;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStudentUsername() {
        return studentUsername;
    }

    public void setStudentUsername(String studentUsername) {
        this.studentUsername = studentUsername;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    // ─── CSV Serialization ──────────────────────────────────────────────────────

    /**
     * Serializes this Application to a CSV line for file storage.
     *
     * <p>Format: {@code applicationId,studentUsername,courseId,status,applyTime,statement}</p>
     *
     * <p><b>Note:</b> {@code statement} is intentionally placed as the last field
     * so that {@link #fromCSVLine(String)} can use a limit-6 split and preserve
     * any commas that may appear inside the statement text.</p>
     *
     * @return a comma-separated string representing this application
     */
    public String toCSVLine() {
        return (applicationId   != null ? applicationId   : "") + ","
             + (studentUsername != null ? studentUsername : "") + ","
             + (courseId        != null ? courseId        : "") + ","
             + (status          != null ? status.name()   : "") + ","
             + (applyTime       != null ? applyTime       : "") + ","
             + (statement       != null ? statement       : "");
    }

    /**
     * Deserializes a CSV line back into an {@link Application} object.
     *
     * <p>Format: {@code applicationId,studentUsername,courseId,status,applyTime,statement}</p>
     *
     * @param csvLine a comma-separated string in the format produced by {@link #toCSVLine()}
     * @return the reconstructed {@link Application}, or {@code null} if the line is
     *         blank, a comment (starts with {@code #}), or malformed
     */
    public static Application fromCSVLine(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty() || csvLine.trim().startsWith("#")) {
            return null;
        }

        // Limit to 6 tokens so the statement (last field) may contain commas
        String[] parts = csvLine.trim().split(",", 6);
        if (parts.length < 6) {
            return null;
        }

        Application app = new Application();
        app.setApplicationId(parts[0].trim());
        app.setStudentUsername(parts[1].trim());
        app.setCourseId(parts[2].trim());

        try {
            app.setStatus(Status.valueOf(parts[3].trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            // Unknown status value — skip this record
            return null;
        }

        app.setApplyTime(parts[4].trim());
        app.setStatement(parts[5].trim());

        return app;
    }

    // ─── Object overrides ───────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Application{"
                + "applicationId='" + applicationId + '\''
                + ", studentUsername='" + studentUsername + '\''
                + ", courseId='" + courseId + '\''
                + ", status=" + status
                + ", applyTime='" + applyTime + '\''
                + ", statement='" + statement + '\''
                + '}';
    }
}
