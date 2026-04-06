package com.group55.ta.dao;

import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.JsonFileUtil;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DAO for application JSON files.
 */
public class ApplicationDao {
    private static final Object LOCK = new Object();

    public ApplicationRecord create(String applicantId, String jobId, String coverLetter) {
        synchronized (LOCK) {
            if (hasApplied(applicantId, jobId)) {
                throw new IllegalStateException("You have already applied for this job.");
            }
            ApplicationRecord record = new ApplicationRecord();
            record.setApplicationId(nextApplicationId());
            record.setApplicantId(applicantId);
            record.setJobId(jobId);
            record.setCoverLetter(coverLetter);
            record.setAppliedAt(DateTimeUtil.nowIso());
            record.setStatus("pending");
            record.setReviewedAt(null);
            record.setReviewNote(null);
            save(record);
            return record;
        }
    }

    public void save(ApplicationRecord record) {
        synchronized (LOCK) {
            Path file = AppPaths.applications().resolve(record.getApplicationId() + ".json");
            JsonFileUtil.write(file, record);
        }
    }

    public Optional<ApplicationRecord> findById(String applicationId) {
        synchronized (LOCK) {
            Path file = AppPaths.applications().resolve(applicationId + ".json");
            return JsonFileUtil.read(file, ApplicationRecord.class);
        }
    }

    public List<ApplicationRecord> listAll() {
        synchronized (LOCK) {
            List<ApplicationRecord> records = JsonFileUtil.readAll(AppPaths.applications(), ApplicationRecord.class);
            records.sort(Comparator.comparing(ApplicationRecord::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed());
            return records;
        }
    }

    public List<ApplicationRecord> listByApplicant(String applicantId) {
        return listAll().stream()
                .filter(record -> applicantId.equals(record.getApplicantId()))
                .collect(Collectors.toList());
    }

    public List<ApplicationRecord> listByJob(String jobId) {
        return listAll().stream()
                .filter(record -> jobId.equals(record.getJobId()))
                .collect(Collectors.toList());
    }

    public boolean hasApplied(String applicantId, String jobId) {
        return listAll().stream()
                .anyMatch(record -> applicantId.equals(record.getApplicantId()) && jobId.equals(record.getJobId()));
    }

    private String nextApplicationId() {
        List<ApplicationRecord> records = listAll();
        int max = 0;
        for (ApplicationRecord record : records) {
            if (record.getApplicationId() == null) {
                continue;
            }
            String[] parts = record.getApplicationId().split("_");
            if (parts.length == 2) {
                try {
                    max = Math.max(max, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "APP_" + String.format("%03d", max + 1);
    }
}
