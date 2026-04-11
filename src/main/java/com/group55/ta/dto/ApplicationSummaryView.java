package com.group55.ta.dto;

import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.Job;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model for TA application history.
 */
public class ApplicationSummaryView {
    private final ApplicationRecord application;
    private final Job job;
    private final int matchScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;

    public ApplicationSummaryView(ApplicationRecord application,
                                  Job job,
                                  int matchScore,
                                  List<String> matchedSkills,
                                  List<String> missingSkills) {
        this.application = application;
        this.job = job;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills == null ? new ArrayList<>() : new ArrayList<>(matchedSkills);
        this.missingSkills = missingSkills == null ? new ArrayList<>() : new ArrayList<>(missingSkills);
    }

    public ApplicationRecord getApplication() {
        return application;
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
}
