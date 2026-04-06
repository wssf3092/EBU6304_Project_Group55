package com.group55.ta.dto;

import com.group55.ta.model.Job;
import com.group55.ta.util.ValidationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model for TA-facing job list and detail pages.
 */
public class JobListingView {
    private final Job job;
    private final int matchScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final boolean applied;
    private final boolean profileComplete;
    private final boolean available;
    private final String applyDisabledReason;

    public JobListingView(Job job,
                          int matchScore,
                          List<String> matchedSkills,
                          List<String> missingSkills,
                          boolean applied,
                          boolean profileComplete,
                          boolean available,
                          String applyDisabledReason) {
        this.job = job;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills == null ? new ArrayList<>() : new ArrayList<>(matchedSkills);
        this.missingSkills = missingSkills == null ? new ArrayList<>() : new ArrayList<>(missingSkills);
        this.applied = applied;
        this.profileComplete = profileComplete;
        this.available = available;
        this.applyDisabledReason = ValidationUtil.trim(applyDisabledReason);
    }

    public Job getJob() {
        return job;
    }

    public int getMatchScore() {
        return matchScore;
    }

    public List<String> getMatchedSkills() {
        return matchedSkills;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public boolean isApplied() {
        return applied;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getApplyDisabledReason() {
        return applyDisabledReason;
    }

    public boolean isActionEnabled() {
        return available && profileComplete && !applied && ValidationUtil.isBlank(applyDisabledReason);
    }
}
