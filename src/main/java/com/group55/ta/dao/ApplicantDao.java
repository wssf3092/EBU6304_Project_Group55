package com.group55.ta.dao;

import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.JsonFileUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * DAO for applicant profile JSON files.
 */
public class ApplicantDao {
    private static final Object LOCK = new Object();

    public Optional<ApplicantProfile> findByUserId(String userId) {
        synchronized (LOCK) {
            Path file = AppPaths.applicants().resolve(userId + ".json");
            return JsonFileUtil.read(file, ApplicantProfile.class);
        }
    }

    public void save(ApplicantProfile profile) {
        synchronized (LOCK) {
            Path file = AppPaths.applicants().resolve(profile.getUserId() + ".json");
            JsonFileUtil.write(file, profile);
        }
    }

    public List<ApplicantProfile> listAll() {
        synchronized (LOCK) {
            return JsonFileUtil.readAll(AppPaths.applicants(), ApplicantProfile.class);
        }
    }
}
