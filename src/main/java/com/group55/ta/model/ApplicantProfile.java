package com.group55.ta.model;

import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * TA applicant profile model.
 */
public class ApplicantProfile {
    private String userId;
    private String name;
    private String studentId;
    private String contactEmail;
    private String major;
    private Integer year;
    private List<String> skills = new ArrayList<>();
    private String bio;
    private Integer maxWorkloadHoursPerWeek;
    private String cvFileName;
    private String cvUploadedAt;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills == null ? new ArrayList<>() : skills;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Integer getMaxWorkloadHoursPerWeek() {
        return maxWorkloadHoursPerWeek;
    }

    public void setMaxWorkloadHoursPerWeek(Integer maxWorkloadHoursPerWeek) {
        this.maxWorkloadHoursPerWeek = maxWorkloadHoursPerWeek;
    }

    public String getCvFileName() {
        return cvFileName;
    }

    public void setCvFileName(String cvFileName) {
        this.cvFileName = cvFileName;
    }

    public String getCvUploadedAt() {
        return cvUploadedAt;
    }

    public void setCvUploadedAt(String cvUploadedAt) {
        this.cvUploadedAt = cvUploadedAt;
    }

    public boolean isComplete() {
        return !ValidationUtil.isBlank(userId)
                && !ValidationUtil.isBlank(studentId)
                && !ValidationUtil.isBlank(contactEmail)
                && !ValidationUtil.isBlank(major)
                && year != null
                && year > 0
                && skills != null
                && !skills.isEmpty()
                && !ValidationUtil.isBlank(bio)
                && maxWorkloadHoursPerWeek != null
                && maxWorkloadHoursPerWeek > 0;
    }

    public boolean hasCv() {
        return !ValidationUtil.isBlank(cvFileName);
    }

    public boolean isHasCv() {
        return hasCv();
    }

    public String getDisplayCvUploadedAt() {
        return DateTimeUtil.formatDateTime(cvUploadedAt);
    }
}
