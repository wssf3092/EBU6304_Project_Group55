package com.group55.ta.model;

import java.io.Serializable;

/**
 * Course model representing a course that requires Teaching Assistants.
 *
 * <p>CSV format in {@code data/courses.txt}:
 * {@code courseId,courseName,teacherUsername,taRequired,description}</p>
 *
 * @author Group 55
 */
public class Course implements Serializable {

    private String id;
    private String name;
    private String teacherUsername;
    private String description;
    private int taNeedCount;
    private int currentTaCount;

    /** No-arg constructor. */
    public Course() {
    }

    /**
     * Full constructor matching test expectations (6 parameters).
     *
     * @param id               the unique course ID, e.g. "CS101"
     * @param name             the course name
     * @param teacherUsername   the username of the teacher who owns this course
     * @param description      a short description of the course
     * @param taNeedCount      how many TAs are needed
     * @param currentTaCount   how many TAs are currently approved
     */
    public Course(String id, String name, String teacherUsername, String description, int taNeedCount, int currentTaCount) {
        this.id = id;
        this.name = name;
        this.teacherUsername = teacherUsername;
        this.description = description;
        this.taNeedCount = taNeedCount;
        this.currentTaCount = currentTaCount;
    }

    /**
     * 5-parameter constructor (currentTaCount defaults to 0).
     */
    public Course(String id, String name, String teacherUsername, String description, int taNeedCount) {
        this(id, name, teacherUsername, description, taNeedCount, 0);
    }

    // ---- Getters & Setters ----

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getTaNeedCount() {
        return taNeedCount;
    }

    public void setTaNeedCount(int taNeedCount) {
        this.taNeedCount = taNeedCount;
    }

    public int getCurrentTaCount() {
        return currentTaCount;
    }

    public void setCurrentTaCount(int currentTaCount) {
        this.currentTaCount = currentTaCount;
    }

    @Override
    public String toString() {
        return "Course{id='" + id + "', name='" + name + "', teacher='" + teacherUsername + "'}";
    }
}
