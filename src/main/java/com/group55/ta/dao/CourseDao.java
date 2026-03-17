package com.group55.ta.dao;

import com.group55.ta.model.Course;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for {@link Course} entities.
 *
 * <p>All persistence is delegated to {@link FileStorageUtil}, which reads from
 * and writes to {@code data/courses.txt}. Each non-blank, non-comment line in
 * that file is one CSV-encoded course record (see {@link Course#toCSVLine()}).</p>
 *
 * <p><b>MVC Role:</b> DAO layer (between Model and Controller)</p>
 *
 * @author Group 55 - Dev-C
 * @version Sprint 1
 */
public class CourseDao {

    /** Name of the backing data file relative to the {@code data/} directory. */
    private static final String COURSES_FILE = "courses.txt";

    /**
     * Returns all courses stored in the data file.
     *
     * <p>Blank lines and comment lines (starting with {@code #}) are
     * automatically skipped by {@link Course#fromCSVLine(String)}.</p>
     *
     * @return a {@link List} of all valid {@link Course} objects; never {@code null},
     *         but may be empty if the file is missing or contains no valid records
     */
    public List<Course> findAll() {
        List<Course> courses = new ArrayList<>();
        List<String> lines = FileStorageUtil.readLines(COURSES_FILE);
        if (lines == null) {
            return courses;
        }

        for (String line : lines) {
            Course course = Course.fromCSVLine(line);
            if (course != null) {
                courses.add(course);
            }
        }
        return courses;
    }

    /**
     * Looks up a single {@link Course} by its unique course ID.
     *
     * @param courseId the course ID to search for (case-sensitive)
     * @return the matching {@link Course}, or {@code null} if not found
     */
    public Course findById(String courseId) {
        if (courseId == null || courseId.trim().isEmpty()) {
            return null;
        }

        List<String> lines = FileStorageUtil.readLines(COURSES_FILE);
        if (lines == null) {
            return null;
        }

        for (String line : lines) {
            Course course = Course.fromCSVLine(line);
            if (course != null && courseId.trim().equals(course.getCourseId())) {
                return course;
            }
        }
        return null;
    }

    /**
     * Persists a new {@link Course} by appending its CSV representation to the
     * courses data file.
     *
     * @param course the course to save; must not be {@code null}
     */
    public void save(Course course) {
        if (course == null) {
            System.err.println("[CourseDao] save() called with null course — skipped.");
            return;
        }
        FileStorageUtil.appendToFile(COURSES_FILE, course.toCSVLine());
        System.out.println("[CourseDao] Saved course: " + course.getCourseId()
                + " — " + course.getCourseName());
    }
}
