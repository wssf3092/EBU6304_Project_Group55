package com.group55.ta.model;

/**
 * Course model (transition; Step 7 replaces with Job).
 * Persisted as one JSON file per course under {@code data/courses/}.
 */
public class Course {
    private String courseId;
    private String name;
    /** MO userId who owns this course. */
    private String teacher;
    private String description;
    private int taNeedCount;
    private int currentTaCount;

    /** Filled at runtime for JSP; not persisted (Gson skips {@code transient}). */
    private transient String teacherName;
    private transient int applicantCount;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    /** Alias for JSPs using {@code course.id}. */
    public String getId() {
        return courseId;
    }

    public void setId(String id) {
        this.courseId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    /** Legacy accessor for servlets comparing owner. */
    public String getTeacherUsername() {
        return teacher;
    }

    public void setTeacherUsername(String teacherUsername) {
        this.teacher = teacherUsername;
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

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public int getApplicantCount() {
        return applicantCount;
    }

    public void setApplicantCount(int applicantCount) {
        this.applicantCount = applicantCount;
    }
}
