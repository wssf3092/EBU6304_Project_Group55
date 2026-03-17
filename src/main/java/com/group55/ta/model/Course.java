package com.group55.ta.model;

/**
 * Represents a course in the TA Recruitment System.
 *
 * <p>Each course may require one or more Teaching Assistants (TAs).
 * This class supports CSV-based serialization for file storage via
 * {@link #toCSVLine()} and {@link #fromCSVLine(String)}.</p>
 *
 * <p><b>MVC Role:</b> Model (M layer)</p>
 *
 * @author Group 55 - Dev-B
 * @version Sprint 1
 */
public class Course {

    private String courseId;
    private String courseName;
    private String teacherUsername;
    private String description;
    /** Number of TA positions required for this course. */
    private int taRequired;

    // ─── Constructors ───────────────────────────────────────────────────────────

    /**
     * Default no-arg constructor.
     */
    public Course() {
    }

    /**
     * Full-argument constructor.
     *
     * @param courseId        the unique identifier of the course (e.g. {@code "CS101"})
     * @param courseName      the human-readable name of the course
     * @param teacherUsername the username of the teacher responsible for this course
     * @param description     a short description of the course content
     * @param taRequired      the number of TA positions needed
     */
    public Course(String courseId, String courseName, String teacherUsername,
                  String description, int taRequired) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacherUsername = teacherUsername;
        this.description = description;
        this.taRequired = taRequired;
    }

    // ─── Getters & Setters ──────────────────────────────────────────────────────

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherUsername() {
        return teacherUsername;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacherUsername = teacherUsername;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaRequired() {
        return taRequired;
    }

    public void setTaRequired(int taRequired) {
        this.taRequired = taRequired;
    }

    // ─── CSV Serialization ──────────────────────────────────────────────────────

    /**
     * Serializes this Course to a CSV line for file storage.
     *
     * <p>Format: {@code courseId,courseName,teacherUsername,taRequired,description}</p>
     *
     * <p><b>Note:</b> Field values must not contain commas. The {@code description}
     * field is placed last and captured with a limit split so it may safely contain
     * any characters other than a newline.</p>
     *
     * @return a comma-separated string representing this course
     */
    public String toCSVLine() {
        return (courseId        != null ? courseId        : "") + ","
             + (courseName      != null ? courseName      : "") + ","
             + (teacherUsername != null ? teacherUsername : "") + ","
             + taRequired + ","
             + (description     != null ? description     : "");
    }

    /**
     * Deserializes a CSV line back into a {@link Course} object.
     *
     * <p>Format: {@code courseId,courseName,teacherUsername,taRequired,description}</p>
     *
     * @param csvLine a comma-separated string in the format produced by {@link #toCSVLine()}
     * @return the reconstructed {@link Course}, or {@code null} if the line is
     *         blank, a comment (starts with {@code #}), or malformed
     */
    public static Course fromCSVLine(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty() || csvLine.trim().startsWith("#")) {
            return null;
        }

        // Limit to 5 tokens so description may contain commas
        String[] parts = csvLine.trim().split(",", 5);
        if (parts.length < 5) {
            return null;
        }

        Course course = new Course();
        course.setCourseId(parts[0].trim());
        course.setCourseName(parts[1].trim());
        course.setTeacherUsername(parts[2].trim());

        try {
            course.setTaRequired(Integer.parseInt(parts[3].trim()));
        } catch (NumberFormatException e) {
            // Non-numeric taRequired — skip this record
            return null;
        }

        course.setDescription(parts[4].trim());

        return course;
    }

    // ─── Object overrides ───────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "Course{"
                + "courseId='" + courseId + '\''
                + ", courseName='" + courseName + '\''
                + ", teacherUsername='" + teacherUsername + '\''
                + ", taRequired=" + taRequired
                + ", description='" + description + '\''
                + '}';
    }
}
