package com.group55.ta.dao;

import com.group55.ta.model.Job;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.JsonFileUtil;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * DAO for job posting JSON files.
 */
public class JobDao {
    private static final Object LOCK = new Object();

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
        }
    }

    public Optional<Job> findById(String jobId) {
        synchronized (LOCK) {
            Path file = AppPaths.jobs().resolve(jobId + ".json");
            return JsonFileUtil.read(file, Job.class);
        }
    }

    public List<Job> listAll() {
        synchronized (LOCK) {
            List<Job> jobs = JsonFileUtil.readAll(AppPaths.jobs(), Job.class);
            jobs.sort(Comparator.comparing(Job::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed());
            return jobs;
        }
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
