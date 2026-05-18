package com.group55.ta.dao;

import com.group55.ta.model.Job;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.JsonFileUtil;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * DAO for job posting JSON files.
 * <p>
 * Maintains a lazily-populated in-memory index ({@code jobId → Job}) so AI matching, dashboards,
 * and admin views avoid repeated whole-directory scans. Writes invalidate the index; the JSON
 * files remain the source of truth.
 */
public class JobDao {
    private static final Object LOCK = new Object();
    private static volatile Map<String, Job> idIndex;

    public Job create(Job job) {
        synchronized (LOCK) {
            job.setJobId(nextJobId());
            job.setStatus("open");
            job.setAcceptedCount(0);
            job.setCreatedAt(DateTimeUtil.nowIso());
            save(job);
            return job;
        }
    }

    public void save(Job job) {
        synchronized (LOCK) {
            Path file = AppPaths.jobs().resolve(job.getJobId() + ".json");
            JsonFileUtil.write(file, job);
            invalidateIndex();
        }
    }

    public Optional<Job> findById(String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            return Optional.empty();
        }
        Job cached = ensureIndex().get(jobId);
        if (cached != null) {
            return Optional.of(cached);
        }
        synchronized (LOCK) {
            Path file = AppPaths.jobs().resolve(jobId + ".json");
            return JsonFileUtil.read(file, Job.class);
        }
    }

    public List<Job> listAll() {
        List<Job> jobs = new ArrayList<>(ensureIndex().values());
        jobs.sort(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
        return jobs;
    }

    private Map<String, Job> ensureIndex() {
        Map<String, Job> snapshot = idIndex;
        if (snapshot != null) {
            return snapshot;
        }
        synchronized (LOCK) {
            if (idIndex == null) {
                Map<String, Job> built = new HashMap<>();
                for (Job job : JsonFileUtil.readAll(AppPaths.jobs(), Job.class)) {
                    if (job.getJobId() != null) {
                        built.put(job.getJobId(), job);
                    }
                }
                idIndex = built;
            }
            return idIndex;
        }
    }

    private static void invalidateIndex() {
        idIndex = null;
    }

    public boolean closeJob(String jobId) {
        synchronized (LOCK) {
            Optional<Job> jobOpt = findById(jobId);
            if (!jobOpt.isPresent()) {
                return false;
            }
            Job job = jobOpt.get();
            job.setStatus("closed");
            save(job);
            return true;
        }
    }

    private String nextJobId() {
        List<Job> jobs = listAll();
        int max = 0;
        for (Job job : jobs) {
            if (job.getJobId() == null) {
                continue;
            }
            String[] parts = job.getJobId().split("_");
            if (parts.length == 2) {
                try {
                    max = Math.max(max, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return "JOB_" + String.format("%03d", max + 1);
    }
}
