package com.group55.ta.dto;

import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.User;

/**
 * Read model for Admin user management.
 */
public class UserOverviewView {
    private final User user;
    private final ApplicantProfile profile;
    private final int acceptedHours;
    private final boolean hasProfile;
    private final boolean hasCv;

    public UserOverviewView(User user, ApplicantProfile profile, int acceptedHours, boolean hasProfile, boolean hasCv) {
        this.user = user;
        this.profile = profile;
        this.acceptedHours = acceptedHours;
        this.hasProfile = hasProfile;
        this.hasCv = hasCv;
    }

    public User getUser() {
        return user;
    }

    public ApplicantProfile getProfile() {
        return profile;
    }

    public int getAcceptedHours() {
        return acceptedHours;
    }

    public boolean isHasProfile() {
        return hasProfile;
    }

    public boolean isHasCv() {
        return hasCv;
    }
}
