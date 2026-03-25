package com.group55.ta.model;

import java.io.Serializable;

/**
 * Application model representing a student's TA application for a course.
 *
 * <p>CSV format in {@code data/applications.txt}:
 * {@code applicationId,studentUsername,courseId,status,applyTime,statement}</p>
 *
 * @author Group 55
 */
public class Application implements Serializable {

    public enum Status { PENDING, APPROVED, REJECTED }

    private String applicationId;
    private String studentUsername;
    private String courseId;
    private Status status;
    private String applyTime;
    private String statement;

    /** No-arg constructor. */
    public Application() {
    }

    /**
     * Full constructor.
     *
     * @param applicationId   the unique application ID
     * @param studentUsername  the username of the applying student
     * @param courseId         the ID of the course being applied to
     * @param status           the current application status
     * @param applyTime        the timestamp when the application was submitted
     * @param statement        the student's personal statement
     */
    public Application(String applicationId, String studentUsername, String courseId,
                       Status status, String applyTime, String statement) {
        this.applicationId = applicationId;
        this.studentUsername = studentUsername;
        this.courseId = courseId;
        this.status = status;
        this.applyTime = applyTime;
        this.statement = statement;
    }

    // ---- Getters & Setters ----

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

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    @Override
    public String toString() {
        return "Application{id='" + applicationId + "', student='" + studentUsername +
               "', course='" + courseId + "', status=" + status + "}";
    }
}
