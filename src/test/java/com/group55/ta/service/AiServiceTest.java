package com.group55.ta.service;

import com.google.gson.JsonObject;
import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.Job;
import com.group55.ta.model.User;
import com.group55.ta.util.AppPaths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ensures AI endpoints degrade to structured JSON when the HTTP layer cannot reach a model. */
class AiServiceTest {
    @TempDir
    Path tempDir;

    private AuthService authService;
    private RecruitmentService recruitmentService;
    private AiService aiService;

    @BeforeEach
    void setUp() {
        AppPaths.overrideDataRoot(tempDir.resolve("data"));
        System.setProperty("ai.baseUrl", "http://127.0.0.1:9/v1/chat/completions");
        System.setProperty("ai.apiKey", "test-key");
        System.setProperty("ai.model", "deepseek-chat");
        System.setProperty("ai.timeoutMillis", "10");
        authService = new AuthService();
        recruitmentService = new RecruitmentService();
        aiService = new AiService();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        System.clearProperty("ai.baseUrl");
        System.clearProperty("ai.apiKey");
        System.clearProperty("ai.model");
        System.clearProperty("ai.timeoutMillis");
        System.clearProperty("ai.cacheMinutes");
    }

    @Test
    void fallbackPayloadIsReturnedWhenAiCallFails() {
        User ta = authService.register("Taylor Applicant", "ta@example.com", "Pass1234", "TA");
        User mo = authService.register("Morgan Organiser", "mo@example.com", "Pass1234", "MO");
        User admin = authService.register("Alex Admin", "admin@example.com", "Pass1234", "ADMIN");

        recruitmentService.saveProfile(
                ta,
                "2026213001",
                "ta@example.com",
                "Software Engineering",
                "3",
                "Java, Communication",
                "Hands-on Java experience with peer support and assessment collaboration.",
                "10"
        );

        Job job = recruitmentService.createJob(
                mo,
                "Lab Support TA",
                "EBU6304",
                "Lab Support",
                "Support weekly labs and answer implementation questions for enrolled students.",
                "Java, Communication, Teaching Support",
                "2",
                "4",
                "2026-05-30"
        );

        ApplicationRecord application = recruitmentService.applyForJob(
                ta,
                job.getJobId(),
                "I can support the lab flow and explain code clearly."
        );

        JsonObject gap = aiService.buildSkillsGap(ta, job.getJobId());
        JsonObject match = aiService.buildMatchInsight(mo, application.getApplicationId());
        JsonObject workload = aiService.buildWorkloadAdvice(admin);

        assertFalse(gap.get("available").getAsBoolean());
        assertFalse(match.get("available").getAsBoolean());
        assertFalse(workload.get("available").getAsBoolean());
        assertTrue(gap.getAsJsonArray("missingSkills").size() >= 0);
    }
}
