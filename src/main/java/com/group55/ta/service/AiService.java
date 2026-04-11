package com.group55.ta.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.group55.ta.dao.ApplicantDao;
import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.JobDao;
import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.Job;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.model.WorkloadEntry;
import com.group55.ta.util.AiCacheUtil;
import com.group55.ta.util.AiConfig;
import com.group55.ta.util.AiHttpClient;
import com.group55.ta.util.AppPaths;
import com.group55.ta.util.CvFileUtil;
import com.group55.ta.util.CvTextExtractor;
import com.group55.ta.util.SkillUtil;
import com.group55.ta.util.ValidationUtil;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Step 10 orchestration: skills-gap (TA), match insight (MO/Admin), workload advice (Admin).
 * When {@link AiConfig#isConfigured()} is false or HTTP fails, methods still return JSON with
 * {@code available:false} and rule-based fields so the UI can render without throwing.
 */
public class AiService {
    private final RecruitmentService recruitmentService = new RecruitmentService();
    private final ApplicantDao applicantDao = new ApplicantDao();
    private final JobDao jobDao = new JobDao();
    private final ApplicationDao applicationDao = new ApplicationDao();
    private final AiConfig config = new AiConfig();
    private final AiHttpClient client = new AiHttpClient(config);

    public JsonObject buildSkillsGap(User requester, String jobId) {
        Job job = jobDao.findById(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        ApplicantProfile profile = applicantDao.findByUserId(requester.getUserId())
                .orElseThrow(() -> new IllegalStateException("Create your applicant profile before running skills analysis."));

        List<String> matchedSkills = SkillUtil.matchedSkills(profile.getSkills(), job.getRequiredSkills());
        List<String> missingSkills = SkillUtil.missingSkills(profile.getSkills(), job.getRequiredSkills());

        JsonObject result = new JsonObject();
        result.addProperty("jobId", job.getJobId());
        result.addProperty("structuredScore", SkillUtil.baseMatchScore(profile.getSkills(), job.getRequiredSkills()));
        result.add("matchedSkills", toArray(matchedSkills));
        result.add("missingSkills", toArray(missingSkills));
        result.add("priorityGaps", defaultGapAdvice(missingSkills));
        result.addProperty("summary", missingSkills.isEmpty()
                ? "Your current profile already covers the required skills."
                : "Focus on the missing skills listed below before the application deadline.");
        result.addProperty("available", false);
        result.addProperty("notice", "AI analysis is unavailable. Showing structured guidance only.");

        if (!config.isConfigured()) {
            return result;
        }

        Path cacheFile = AppPaths.aiCache().resolve("gap_" + job.getJobId() + "_" + requester.getUserId() + ".json");
        JsonObject aiPayload = cachedOrFetch(cacheFile, Duration.ofMinutes(config.getCacheMinutes()), () ->
                client.requestJson(
                        "You are assisting a teaching assistant recruitment platform. Return valid json only.",
                        "Evaluate missing skills for a TA applicant.\n"
                                + "Return json with fields: summary (string), priorityGaps (array of objects with skill, why, suggestion).\n"
                                + "Example json: {\"summary\":\"...\",\"priorityGaps\":[{\"skill\":\"...\",\"why\":\"...\",\"suggestion\":\"...\"}]}\n"
                                + "Job title: " + job.getTitle() + "\n"
                                + "Module: " + job.getModule() + "\n"
                                + "Required skills: " + String.join(", ", job.getRequiredSkills()) + "\n"
                                + "Applicant skills: " + String.join(", ", profile.getSkills()) + "\n"
                                + "Applicant bio: " + profile.getBio()
                ));
        mergeGapResult(result, aiPayload);
        return result;
    }

    public JsonObject buildMatchInsight(User requester, String applicationId) {
        ApplicationRecord application = applicationDao.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        Job job = jobDao.findById(application.getJobId()).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (Role.MO.name().equalsIgnoreCase(requester.getRole()) && !requester.getUserId().equals(job.getMoId())) {
            throw new IllegalStateException("You can only inspect applicants for your own jobs.");
        }

        ApplicantProfile profile = applicantDao.findByUserId(application.getApplicantId())
                .orElseThrow(() -> new IllegalStateException("Applicant profile is missing."));
        List<String> matchedSkills = SkillUtil.matchedSkills(profile.getSkills(), job.getRequiredSkills());
        List<String> missingSkills = SkillUtil.missingSkills(profile.getSkills(), job.getRequiredSkills());
        int structuredScore = SkillUtil.baseMatchScore(profile.getSkills(), job.getRequiredSkills());

        JsonObject result = new JsonObject();
        result.addProperty("applicationId", applicationId);
        result.addProperty("jobId", job.getJobId());
        result.addProperty("applicantId", application.getApplicantId());
        result.addProperty("structuredScore", structuredScore);
        result.addProperty("aiScore", structuredScore);
        result.addProperty("combinedScore", structuredScore);
        result.add("matchedSkills", toArray(matchedSkills));
        result.add("missingSkills", toArray(missingSkills));
        result.add("strengths", toArray(matchedSkills.stream()
                .map(skill -> skill + " aligns directly with the required skill set.")
                .collect(Collectors.toList())));
        result.add("risks", toArray(missingSkills.stream()
                .map(skill -> skill + " is not currently evidenced in the applicant profile.")
                .collect(Collectors.toList())));
        result.addProperty("summary", matchedSkills.isEmpty()
                ? "The current profile does not show a strong direct skills overlap."
                : "The applicant covers part of the required skills and needs a full review.");
        result.addProperty("available", false);
        result.addProperty("notice", "AI analysis is unavailable. Showing structured evidence only.");

        if (!config.isConfigured()) {
            return result;
        }

        String cvText = CvFileUtil.findCv(application.getApplicantId()).map(CvTextExtractor::extractText).orElse("");
        Path cacheFile = AppPaths.aiCache().resolve("match_" + job.getJobId() + "_" + application.getApplicantId() + ".json");
        JsonObject aiPayload = cachedOrFetch(cacheFile, Duration.ofMinutes(config.getCacheMinutes()), () ->
                client.requestJson(
                        "You are assisting a teaching assistant recruitment platform. Return valid json only.",
                        "Assess the applicant-job match.\n"
                                + "Return json with fields: aiScore (integer 0-100), summary (string), strengths (array of strings), risks (array of strings).\n"
                                + "Example json: {\"aiScore\":84,\"summary\":\"...\",\"strengths\":[\"...\"],\"risks\":[\"...\"]}\n"
                                + "Required skills: " + String.join(", ", job.getRequiredSkills()) + "\n"
                                + "Job description: " + job.getDescription() + "\n"
                                + "Applicant skills: " + String.join(", ", profile.getSkills()) + "\n"
                                + "Applicant bio: " + profile.getBio() + "\n"
                                + "Applicant CV text: " + (ValidationUtil.isBlank(cvText) ? "No extractable CV text available." : cvText)
                ));
        mergeMatchResult(result, aiPayload);
        return result;
    }

    public JsonObject buildWorkloadAdvice(User requester) {
        if (!Role.ADMIN.name().equalsIgnoreCase(requester.getRole())) {
            throw new IllegalStateException("Only administrators can request workload advice.");
        }

        List<WorkloadEntry> workloadEntries = recruitmentService.buildWorkloadEntries();
        List<Job> openJobs = recruitmentService.listJobsForAdmin().stream()
                .map(view -> view.getJob())
                .filter(job -> !job.isClosed())
                .collect(Collectors.toList());

        JsonObject result = new JsonObject();
        result.addProperty("available", false);
        result.addProperty("summary", buildStructuredWorkloadSummary(workloadEntries, openJobs));
        result.add("recommendations", heuristicRecommendations(workloadEntries, openJobs));
        result.addProperty("notice", "AI analysis is unavailable. Showing structured workload guidance only.");

        if (!config.isConfigured()) {
            return result;
        }

        Path cacheFile = AppPaths.aiCache().resolve("workload_balance.json");
        JsonObject aiPayload = cachedOrFetch(cacheFile, Duration.ofMinutes(config.getCacheMinutes()), () ->
                client.requestJson(
                        "You are assisting an academic recruitment admin console. Return valid json only.",
                        "Balance workload across TAs using anonymized data.\n"
                                + "Return json with fields: summary (string), recommendations (array of objects with taId, jobId, reason).\n"
                                + "Example json: {\"summary\":\"...\",\"recommendations\":[{\"taId\":\"...\",\"jobId\":\"...\",\"reason\":\"...\"}]}\n"
                                + "TA workload entries: " + anonymizedWorkload(workloadEntries) + "\n"
                                + "Open jobs: " + openJobs.stream()
                                .map(job -> job.getJobId() + " [" + job.getTitle() + "] skills=" + String.join("/", job.getRequiredSkills())
                                        + " hours=" + job.getWorkloadHoursPerWeek())
                                .collect(Collectors.joining("; "))
                ));
        mergeWorkloadResult(result, aiPayload);
        return result;
    }

    private JsonObject cachedOrFetch(Path cacheFile, Duration ttl, SupplierWithJson supplier) {
        Optional<JsonObject> cached = AiCacheUtil.readIfValid(cacheFile, ttl);
        if (cached.isPresent()) {
            return cached.get();
        }
        JsonObject payload = supplier.get();
        if (payload != null && payload.has("available") && payload.get("available").getAsBoolean()) {
            AiCacheUtil.write(cacheFile, payload);
        }
        return payload == null ? new JsonObject() : payload;
    }

    private void mergeGapResult(JsonObject target, JsonObject aiPayload) {
        if (!aiPayload.has("available") || !aiPayload.get("available").getAsBoolean()) {
            return;
        }
        if (aiPayload.has("summary")) {
            target.addProperty("summary", aiPayload.get("summary").getAsString());
        }
        if (aiPayload.has("priorityGaps") && aiPayload.get("priorityGaps").isJsonArray()) {
            target.add("priorityGaps", aiPayload.getAsJsonArray("priorityGaps"));
        }
        target.addProperty("available", true);
        target.addProperty("notice", "AI-generated guidance is shown for reference only. Successful runs are cached locally.");
    }

    private void mergeMatchResult(JsonObject target, JsonObject aiPayload) {
        if (!aiPayload.has("available") || !aiPayload.get("available").getAsBoolean()) {
            return;
        }
        int structuredScore = target.get("structuredScore").getAsInt();
        int aiScore = aiPayload.has("aiScore") && aiPayload.get("aiScore").isJsonPrimitive()
                ? safeInt(aiPayload.get("aiScore"), structuredScore)
                : structuredScore;
        target.addProperty("aiScore", aiScore);
        target.addProperty("combinedScore", (int) Math.round(structuredScore * 0.65 + aiScore * 0.35));
        if (aiPayload.has("summary")) {
            target.addProperty("summary", aiPayload.get("summary").getAsString());
        }
        if (aiPayload.has("strengths") && aiPayload.get("strengths").isJsonArray()) {
            target.add("strengths", aiPayload.getAsJsonArray("strengths"));
        }
        if (aiPayload.has("risks") && aiPayload.get("risks").isJsonArray()) {
            target.add("risks", aiPayload.getAsJsonArray("risks"));
        }
        target.addProperty("available", true);
        target.addProperty("notice", "AI-generated interpretation is shown for reference only. Successful runs are cached locally.");
    }

    private void mergeWorkloadResult(JsonObject target, JsonObject aiPayload) {
        if (!aiPayload.has("available") || !aiPayload.get("available").getAsBoolean()) {
            return;
        }
        if (aiPayload.has("summary")) {
            target.addProperty("summary", aiPayload.get("summary").getAsString());
        }
        if (aiPayload.has("recommendations") && aiPayload.get("recommendations").isJsonArray()) {
            target.add("recommendations", aiPayload.getAsJsonArray("recommendations"));
        }
        target.addProperty("available", true);
        target.addProperty("notice", "AI-generated workload advice is shown for reference only. Successful runs are cached locally.");
    }

    private JsonArray defaultGapAdvice(List<String> missingSkills) {
        JsonArray array = new JsonArray();
        for (String skill : missingSkills) {
            JsonObject item = new JsonObject();
            item.addProperty("skill", skill);
            item.addProperty("why", "This skill is listed in the job requirements but not yet evidenced in the current profile.");
            item.addProperty("suggestion", "Add concrete coursework, project evidence, or practice examples related to " + skill + ".");
            array.add(item);
        }
        return array;
    }

    private String buildStructuredWorkloadSummary(List<WorkloadEntry> entries, List<Job> openJobs) {
        double average = entries.stream().mapToInt(WorkloadEntry::getTotalHours).average().orElse(0);
        long overloaded = entries.stream().filter(WorkloadEntry::isOverload).count();
        long underloaded = entries.stream().filter(entry -> "underload".equalsIgnoreCase(entry.getLoadStatus())).count();
        return "Average workload is " + String.format("%.1f", average) + " hours per week. "
                + overloaded + " TA(s) are over capacity and " + underloaded + " TA(s) are under-allocated. "
                + openJobs.size() + " position(s) remain available for redistribution.";
    }

    private JsonArray heuristicRecommendations(List<WorkloadEntry> entries, List<Job> openJobs) {
        JsonArray array = new JsonArray();
        List<WorkloadEntry> underloaded = entries.stream()
                .filter(entry -> "underload".equalsIgnoreCase(entry.getLoadStatus()))
                .sorted(Comparator.comparing(WorkloadEntry::getTotalHours))
                .collect(Collectors.toList());

        for (WorkloadEntry entry : underloaded) {
            ApplicantProfile profile = recruitmentService.findProfile(entry.getUserId()).orElse(null);
            if (profile == null) {
                continue;
            }
            Job bestJob = openJobs.stream()
                    .max(Comparator.comparingInt(job -> SkillUtil.baseMatchScore(profile.getSkills(), job.getRequiredSkills())))
                    .orElse(null);
            if (bestJob == null) {
                continue;
            }
            int score = SkillUtil.baseMatchScore(profile.getSkills(), bestJob.getRequiredSkills());
            if (score <= 0) {
                continue;
            }
            JsonObject item = new JsonObject();
            item.addProperty("taId", entry.getUserId());
            item.addProperty("jobId", bestJob.getJobId());
            item.addProperty("reason", entry.getUserId() + " is currently carrying " + entry.getTotalHours()
                    + "h/week and shows a " + score + "% structured skill match for " + bestJob.getTitle() + ".");
            array.add(item);
            if (array.size() >= 4) {
                break;
            }
        }
        return array;
    }

    private String anonymizedWorkload(List<WorkloadEntry> entries) {
        return entries.stream()
                .map(entry -> entry.getUserId() + ": total=" + entry.getTotalHours() + ", max=" + entry.getMaxHours())
                .collect(Collectors.joining("; "));
    }

    private JsonArray toArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }
        for (String value : values) {
            array.add(value);
        }
        return array;
    }

    private int safeInt(JsonElement element, int fallback) {
        try {
            return Math.max(0, Math.min(100, element.getAsInt()));
        } catch (Exception ex) {
            return fallback;
        }
    }

    @FunctionalInterface
    private interface SupplierWithJson {
        JsonObject get();
    }
}
