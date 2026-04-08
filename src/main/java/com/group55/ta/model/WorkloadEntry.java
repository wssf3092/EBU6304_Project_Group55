package com.group55.ta.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Workload projection entry for Admin view (Step 9).
 * {@link #loadStatus} values are assigned in {@link com.group55.ta.service.RecruitmentService#buildWorkloadEntries()}.
 */
public class WorkloadEntry {

    public static final String LOAD_OVERLOAD = "overload";
    public static final String LOAD_BALANCED = "balanced";
    public static final String LOAD_UNDERLOAD = "underload";

    private String userId;
    private String name;
    private List<String> acceptedJobs = new ArrayList<>();
    private int totalHours;
    private int maxHours;
    private String loadStatus;

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

    public List<String> getAcceptedJobs() {
        if (acceptedJobs == null) {
            acceptedJobs = new ArrayList<>();
        }
        return acceptedJobs;
    }

    public void setAcceptedJobs(List<String> acceptedJobs) {
        this.acceptedJobs = acceptedJobs == null ? new ArrayList<>() : acceptedJobs;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public int getMaxHours() {
        return maxHours;
    }

    public void setMaxHours(int maxHours) {
        this.maxHours = maxHours;
    }

    public String getLoadStatus() {
        return loadStatus;
    }

    public void setLoadStatus(String loadStatus) {
        this.loadStatus = loadStatus;
    }

    public boolean isOverload() {
        return LOAD_OVERLOAD.equalsIgnoreCase(loadStatus);
    }

    public boolean isBalanced() {
        return LOAD_BALANCED.equalsIgnoreCase(loadStatus);
    }

    public boolean isUnderload() {
        return LOAD_UNDERLOAD.equalsIgnoreCase(loadStatus);
    }
}
