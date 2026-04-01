package com.group55.ta.dao;

import com.group55.ta.model.Application;
import com.group55.ta.model.Application.Status;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.JsonFileUtil;
import com.group55.ta.util.ValidationUtil;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Applications as {@code data/applications/{applicationId}.json}.
 */
public class ApplicationDao {
    private static final Object LOCK = new Object();

    public Application create(String applicantId, String courseId, String statement) {
        synchronized (LOCK) {
            if (hasApplied(applicantId, courseId)) {
                throw new IllegalStateException("Already applied for this course.");
            }
            Application app = new Application();
            app.setApplicationId(nextApplicationId());
            app.setApplicantId(applicantId);
            app.setCourseId(courseId);
            app.setStatement(statement);
            app.setStatusEnum(Status.PENDING);
            app.setAppliedAt(DateTimeUtil.nowIso());
            save(app);
            return app;
        }
    }

    public void save(Application application) {
        synchronized (LOCK) {
            Path file = AppPaths.applications().resolve(application.getApplicationId() + ".json");
            JsonFileUtil.write(file, application);
        }
    }

    public Optional<Application> findById(String applicationId) {
        synchronized (LOCK) {
            if (ValidationUtil.isBlank(applicationId)) {
                return Optional.empty();
            }
            Path file = AppPaths.applications().resolve(applicationId + ".json");
            return JsonFileUtil.read(file, Application.class);
        }
    }

    public List<Application> findAll() {
        synchronized (LOCK) {
            List<Application> apps = JsonFileUtil.readAll(AppPaths.applications(), Application.class);
            apps.sort(Comparator.comparing(Application::getAppliedAt, Comparator.nullsLast(String::compareTo)).reversed());
            return apps;
        }
    }

    public List<Application> findByApplicant(String applicantId) {
        if (applicantId == null) {
            return new java.util.ArrayList<>();
        }
        return findAll().stream()
                .filter(a -> applicantId.equals(a.getApplicantId()))
                .collect(Collectors.toList());
    }

    /** @deprecated use {@link #findByApplicant(String)} */
    public List<Application> findByStudentUsername(String username) {
        return findByApplicant(username);
    }

    public List<Application> findByCourseId(String courseId) {
        if (courseId == null) {
            return new java.util.ArrayList<>();
        }
        return findAll().stream()
                .filter(a -> courseId.equals(a.getCourseId()))
                .collect(Collectors.toList());
    }

    public boolean updateStatus(String applicationId, Status status) {
        synchronized (LOCK) {
            Optional<Application> opt = findById(applicationId);
            if (!opt.isPresent()) {
                return false;
            }
            Application app = opt.get();
            app.setStatusEnum(status);
            save(app);
            return true;
        }
    }

    public boolean hasApplied(String applicantId, String courseId) {
        return findAll().stream()
                .anyMatch(a -> applicantId.equals(a.getApplicantId()) && courseId.equals(a.getCourseId()));
    }

    private String nextApplicationId() {
        List<Application> apps = findAll();
        int max = 0;
        for (Application app : apps) {
            if (app.getApplicationId() == null) {
                continue;
            }
            String[] parts = app.getApplicationId().split("_");
            if (parts.length == 2) {
                try {
                    max = Math.max(max, Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {
                    // skip
                }
            }
        }
        return "APP_" + String.format("%03d", max + 1);
    }
}
