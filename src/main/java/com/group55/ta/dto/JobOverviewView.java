package com.group55.ta.dto;

import com.group55.ta.model.Job;

/**
 * Read model for MO and Admin job management views.
 */
public class JobOverviewView {
    private final Job job;
    private final int applicationCount;
    private final int pendingCount;
    private final int acceptedCount;

    public JobOverviewView(Job job, int applicationCount, int pendingCount, int acceptedCount) {
        this.job = job;
        this.applicationCount = applicationCount;
        this.pendingCount = pendingCount;
        this.acceptedCount = acceptedCount;
    }

    public Job getJob() {
        return job;
    }

    public int getApplicationCount() {
        return applicationCount;
    }

    public int getPendingCount() {
        return pendingCount;
    }

    public int getAcceptedCount() {
        return acceptedCount;
    }
}
