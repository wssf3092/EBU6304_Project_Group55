package com.group55.ta.dao;

import com.group55.ta.model.Course;
import com.group55.ta.util.FileStorageUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for {@link Course} entities.
 * Persists course data to a CSV text file.
 *
 * <p>CSV line format: {@code courseId,courseName,teacherUsername,taRequired,description}</p>
 *
 * @author Group 55
 */
public class CourseDao {

    private static final String DEFAULT_FILE = "data/courses.txt";
    private final String filePath;

    public CourseDao() {
        this.filePath = DEFAULT_FILE;
    }

    public CourseDao(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Return all courses from the data file.
     */
    public List<Course> findAll() {
        List<String> lines = FileStorageUtil.readLines(filePath);
        if (lines == null) return new ArrayList<>();
        List<Course> courses = new ArrayList<>();
        for (String line : lines) {
            Course c = fromCsvLine(line);
            if (c != null) {
                courses.add(c);
            }
        }
        return courses;
    }

    /**
     * Find a course by its unique ID.
     *
     * @return the {@link Course} if found, otherwise {@code null}
     */
    public Course findById(String courseId) {
        if (courseId == null) return null;
        List<Course> all = findAll();
        for (Course c : all) {
            if (courseId.equals(c.getId())) {
                return c;
            }
        }
        return null;
    }

    /**
     * Find all courses owned by a specific teacher.
     *
     * @param teacherUsername the teacher's username
     * @return list of courses (never null)
     */
    public List<Course> findByTeacher(String teacherUsername) {
        List<Course> result = new ArrayList<>();
        if (teacherUsername == null) return result;
        List<Course> all = findAll();
        for (Course c : all) {
            if (teacherUsername.equals(c.getTeacherUsername())) {
                result.add(c);
            }
        }
        return result;
    }

    /**
     * Save (append) a new course. If the course ID already exists, returns {@code false}.
     *
     * @return {@code true} if saved successfully, {@code false} otherwise
     */
    public boolean save(Course course) {
        if (course == null || course.getId() == null) return false;
        if (findById(course.getId()) != null) return false;

        String line = toCsvLine(course);
        String existing = FileStorageUtil.readFile(filePath);
        if (existing != null && !existing.trim().isEmpty()) {
            return FileStorageUtil.appendToFile(filePath, "\n" + line);
        } else {
            return FileStorageUtil.writeFile(filePath, line);
        }
    }

    /**
     * Clear all data in the file (test utility).
     */
    public void clearAll() {
        FileStorageUtil.writeFile(filePath, "");
    }

    // ---- CSV helpers ----

    private String toCsvLine(Course course) {
        return escape(course.getId()) + "," +
               escape(course.getName()) + "," +
               escape(course.getTeacherUsername()) + "," +
               course.getTaNeedCount() + "," +
               escape(course.getDescription());
    }

    private Course fromCsvLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",", -1);
        if (parts.length < 5) return null;
        // CSV order: courseId,courseName,teacherUsername,taRequired,description
        String id = parts[0].trim();
        String name = parts[1].trim();
        String teacherUsername = parts[2].trim();
        int taNeedCount;
        try {
            taNeedCount = Integer.parseInt(parts[3].trim());
        } catch (NumberFormatException e) {
            taNeedCount = 0;
        }
        String description = parts[4].trim();
        return new Course(id, name, teacherUsername, description, taNeedCount, 0);
    }

    private static String escape(String s) {
        return s == null ? "" : s;
    }
}
