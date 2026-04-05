package com.group55.ta.model;

import com.group55.ta.util.DateTimeUtil;

/**
 * TA application for a course (transition model).
 * Status stored as string for JSON and JSP EL: PENDING, ACCEPTED, REJECTED.
 */
public class Application {

    private String applicationId;
    private String applicantId;
    private String courseId;
    private String status;
    private String appliedAt;
    private String statement;
    /** MO 审核备注（Step 6）；可为空。 */
    private String reviewNote;
    /** ISO 审核时间（Step 6）。 */
    private String reviewedAt;

    private transient String courseName;
    private transient String teacherName;
    private transient String applyDate;
    private transient String applicantName;

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(String applicantId) {
        this.applicantId = applicantId;
    }

    public String getStudentUsername() {
        return applicantId;
    }

    public void setStudentUsername(String studentUsername) {
        this.applicantId = studentUsername;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatusEnum(Status s) {
        this.status = s == null ? null : s.name();
    }

    public Status getStatusEnum() {
        if (status == null) {
            return null;
        }
        try {
            return Status.valueOf(status);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED
    }

    public String getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(String appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getApplyTime() {
        return appliedAt;
    }

    public void setApplyTime(String applyTime) {
        this.appliedAt = applyTime;
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }

    public String getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(String reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getApplyDate() {
        if (applyDate != null) {
            return applyDate;
        }
        return DateTimeUtil.formatDateTime(appliedAt);
    }

    public void setApplyDate(String applyDate) {
        this.applyDate = applyDate;
    }
}
