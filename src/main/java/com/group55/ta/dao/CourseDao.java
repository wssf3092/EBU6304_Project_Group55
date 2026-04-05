package com.group55.ta.dao;

import com.group55.ta.model.Course;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.JsonFileUtil;
import com.group55.ta.util.ValidationUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Courses as {@code data/courses/{courseId}.json}.
 */
public class CourseDao {
    private static final Object LOCK = new Object();

    public List<Course> findAll() {
        synchronized (LOCK) {
            return JsonFileUtil.readAll(AppPaths.courses(), Course.class);
        }
    }

    public Course findById(String courseId) {
        if (ValidationUtil.isBlank(courseId)) {
            return null;
        }
        synchronized (LOCK) {
            Path file = AppPaths.courses().resolve(courseId + ".json");
            return JsonFileUtil.read(file, Course.class).orElse(null);
        }
    }

    public List<Course> findByTeacher(String moUserId) {
        if (moUserId == null) {
            return new ArrayList<>();
        }
        return findAll().stream()
                .filter(c -> moUserId.equals(c.getTeacher()))
                .collect(Collectors.toList());
    }

    public boolean save(Course course) {
        if (course == null) {
            return false;
        }
        String cid = course.getCourseId();
        if (ValidationUtil.isBlank(cid)) {
            cid = course.getId();
        }
        if (ValidationUtil.isBlank(cid)) {
            return false;
        }
        course.setCourseId(cid);
        synchronized (LOCK) {
            Path file = AppPaths.courses().resolve(course.getCourseId() + ".json");
            JsonFileUtil.write(file, course);
            return true;
        }
    }

    public void delete(String courseId) {
        if (ValidationUtil.isBlank(courseId)) {
            return;
        }
        synchronized (LOCK) {
            try {
                Path file = AppPaths.courses().resolve(courseId + ".json");
                java.nio.file.Files.deleteIfExists(file);
            } catch (java.io.IOException e) {
                throw new IllegalStateException("Failed to delete course file", e);
            }
        }
    }
}
