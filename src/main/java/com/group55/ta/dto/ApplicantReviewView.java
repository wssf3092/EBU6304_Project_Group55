package com.group55.ta.dto;

import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Read model for MO applicant review pages.
 */
public class ApplicantReviewView {
    private final ApplicationRecord application;
    private final User applicant;
    private final ApplicantProfile profile;
    private final int matchScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final boolean cvAvailable;

    public ApplicantReviewView(ApplicationRecord application,
                               User applicant,
                               ApplicantProfile profile,
                               int matchScore,
                               List<String> matchedSkills,
                               List<String> missingSkills,
                               boolean cvAvailable) {
        this.application = application;
        this.applicant = applicant;
        this.profile = profile;
        this.matchScore = matchScore;
        this.matchedSkills = matchedSkills == null ? new ArrayList<>() : new ArrayList<>(matchedSkills);
        this.missingSkills = missingSkills == null ? new ArrayList<>() : new ArrayList<>(missingSkills);
        this.cvAvailable = cvAvailable;
    }

    public ApplicationRecord getApplication() {
        return application;
    }

    public User getApplicant() {
        return applicant;
    }

    public ApplicantProfile getProfile() {
        return profile;
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

    public boolean isCvAvailable() {
        return cvAvailable;
    }
}
