package com.group55.ta.model;

import com.group55.ta.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Job posting model.
 */
public class Job {
    private String jobId;
    private String moId;
    private String title;
    private String module;
    private String activityType;
    private String description;
    private List<String> requiredSkills = new ArrayList<>();
    private int quota;
    private int workloadHoursPerWeek;
    private String deadline;
    private String status;
    private int acceptedCount;
    private String createdAt;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getMoId() {
        return moId;
    }

    public void setMoId(String moId) {
        this.moId = moId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getActivityType() {
        return activityType;
    }

    public void setActivityType(String activityType) {
        this.activityType = activityType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills == null ? new ArrayList<>() : requiredSkills;
    }

    public int getQuota() {
        return quota;
    }

    public void setQuota(int quota) {
        this.quota = quota;
    }

    public int getWorkloadHoursPerWeek() {
        return workloadHoursPerWeek;
    }

    public void setWorkloadHoursPerWeek(int workloadHoursPerWeek) {
        this.workloadHoursPerWeek = workloadHoursPerWeek;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }

    public void setAcceptedCount(int acceptedCount) {
        this.acceptedCount = acceptedCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getRemainingQuota() {
        return Math.max(0, quota - acceptedCount);
    }

    public boolean isClosed() {
        return "closed".equalsIgnoreCase(status) || getRemainingQuota() <= 0 || DateTimeUtil.isPastDate(deadline);
    }

    public String getDisplayDeadline() {
        return DateTimeUtil.formatDate(deadline);
    }

    public String getDisplayCreatedAt() {
        return DateTimeUtil.formatDateTime(createdAt);
    }
}
