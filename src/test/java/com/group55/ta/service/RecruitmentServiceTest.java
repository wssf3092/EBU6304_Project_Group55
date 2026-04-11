package com.group55.ta.service;

import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.Job;
import com.group55.ta.model.User;
import com.group55.ta.model.WorkloadEntry;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecruitmentServiceTest {
    @TempDir
    Path tempDir;

    private AuthService authService;
    private RecruitmentService recruitmentService;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir.resolve("data"));
        authService = new AuthService();
        recruitmentService = new RecruitmentService();
    }

    @Test
    void applicationReviewUpdatesJobAndWorkload() {
        User ta = authService.register("Taylor Applicant", "ta@example.com", "Pass1234", "TA");
        User mo = authService.register("Morgan Organiser", "mo@example.com", "Pass1234", "MO");

        recruitmentService.saveProfile(
                ta,
                "2026213001",
                "ta@example.com",
                "Software Engineering",
                "3",
                "Java, Communication, Teaching Support",
                "Strong Java coursework and experience supporting peers through practical lab sessions.",
                "8"
        );

        Job job = recruitmentService.createJob(
                mo,
                "Lab Support TA",
                "EBU6304",
                "Lab Support",
                "Support weekly labs and help maintain delivery consistency across sessions.",
                "Java, Communication, Teaching Support",
                "2",
                "4",
                "2026-05-30"
        );

        ApplicationRecord application = recruitmentService.applyForJob(
                ta,
                job.getJobId(),
                "I can help explain coding tasks and provide dependable student support."
        );

        recruitmentService.reviewApplication(mo, application.getApplicationId(), "accepted", "Relevant fit.");

        Job updatedJob = recruitmentService.findJob(job.getJobId()).orElseThrow(AssertionError::new);
        ApplicationRecord updatedApplication = recruitmentService.findApplication(application.getApplicationId()).orElseThrow(AssertionError::new);
        List<WorkloadEntry> workloads = recruitmentService.buildWorkloadEntries();

        assertEquals(1, updatedJob.getAcceptedCount());
        assertTrue(updatedApplication.isAccepted());
        assertEquals(1, workloads.size());
        assertEquals(4, workloads.get(0).getTotalHours());
    }
}
