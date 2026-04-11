package com.group55.ta.service;

import com.group55.ta.dao.ApplicantDao;
import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.JobDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.dto.ApplicantReviewView;
import com.group55.ta.dto.ApplicationSummaryView;
import com.group55.ta.dto.JobListingView;
import com.group55.ta.dto.JobOverviewView;
import com.group55.ta.dto.UserOverviewView;
import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.ApplicationRecord;
import com.group55.ta.model.Job;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.model.WorkloadEntry;
import com.group55.ta.util.DateTimeUtil;
import com.group55.ta.util.SkillUtil;
import com.group55.ta.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Main business service for profile, job, application, and admin flows.
 */
public class RecruitmentService {
    private static final List<String> SKILL_CATALOG = Arrays.asList(
            "Java", "Python", "Mathematics", "Software Engineering", "Data Analysis",
            "Communication", "English", "Marking", "Lab Support", "Invigilation",
            "Problem Solving", "Git", "Database", "Office Hours", "Teaching Support"
    );
    private static final List<String> ACTIVITY_TYPES = Arrays.asList(
            "Lab Support", "Tutorial Support", "Marking", "Invigilation", "Office Hours", "Project Clinic"
    );

    private final UserDao userDao = new UserDao();
    private final ApplicantDao applicantDao = new ApplicantDao();
    private final JobDao jobDao = new JobDao();
    private final ApplicationDao applicationDao = new ApplicationDao();

    public List<String> getSkillCatalog() {
        return SKILL_CATALOG;
    }

    public List<String> getActivityTypes() {
        return ACTIVITY_TYPES;
    }

    public ApplicantProfile getOrCreateProfile(User user) {
        ApplicantProfile profile = applicantDao.findByUserId(user.getUserId()).orElseGet(ApplicantProfile::new);
        if (ValidationUtil.isBlank(profile.getUserId())) {
            profile.setUserId(user.getUserId());
            profile.setName(user.getName());
            profile.setContactEmail(user.getEmail());
        }
        return profile;
    }

    public ApplicantProfile saveProfile(User user,
                                        String studentId,
                                        String contactEmail,
                                        String major,
                                        String yearValue,
                                        String skillsInput,
                                        String bio,
                                        String maxHoursValue) {
        ApplicantProfile profile = getOrCreateProfile(user);
        int year = parsePositiveInt(yearValue, 8, "Enter a valid year of study.");
        int maxHours = parsePositiveInt(maxHoursValue, 40, "Enter a valid weekly workload limit.");
        List<String> skills = SkillUtil.parseSkills(skillsInput);

        if (ValidationUtil.isBlank(studentId)) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (!ValidationUtil.isValidEmail(contactEmail)) {
            throw new IllegalArgumentException("Enter a valid contact email.");
        }
        if (ValidationUtil.isBlank(major)) {
            throw new IllegalArgumentException("Major is required.");
        }
        if (skills.isEmpty()) {
            throw new IllegalArgumentException("Add at least one skill.");
        }
        if (ValidationUtil.trim(bio).length() < 20) {
            throw new IllegalArgumentException("Profile summary must contain at least 20 characters.");
        }
        if (ValidationUtil.trim(bio).length() > 500) {
            throw new IllegalArgumentException("Profile summary must stay within 500 characters.");
        }

        profile.setUserId(user.getUserId());
        profile.setName(user.getName());
        profile.setStudentId(ValidationUtil.trim(studentId));
        profile.setContactEmail(ValidationUtil.normalizeEmail(contactEmail));
        profile.setMajor(ValidationUtil.trim(major));
        profile.setYear(year);
        profile.setSkills(skills);
        profile.setBio(ValidationUtil.trim(bio));
        profile.setMaxWorkloadHoursPerWeek(maxHours);
        applicantDao.save(profile);
        return profile;
    }

    public void attachCvMetadata(User user, String fileName) {
        ApplicantProfile profile = getOrCreateProfile(user);
        profile.setName(user.getName());
        profile.setContactEmail(ValidationUtil.normalizeEmail(user.getEmail()));
        profile.setCvFileName(fileName);
        profile.setCvUploadedAt(DateTimeUtil.nowIso());
        applicantDao.save(profile);
    }

    public Optional<ApplicantProfile> findProfile(String userId) {
        return applicantDao.findByUserId(userId);
    }

    public Optional<Job> findJob(String jobId) {
        Optional<Job> job = jobDao.findById(jobId);
        job.ifPresent(this::synchronizeJob);
        return job;
    }

    public Optional<ApplicationRecord> findApplication(String applicationId) {
        return applicationDao.findById(applicationId);
    }

    public List<JobListingView> listJobsForApplicant(User user, String keyword, String skillFilter, String statusFilter) {
        ApplicantProfile profile = applicantDao.findByUserId(user.getUserId()).orElse(null);
        String query = ValidationUtil.trim(keyword).toLowerCase(Locale.ROOT);
        String skill = ValidationUtil.trim(skillFilter).toLowerCase(Locale.ROOT);
        String status = ValidationUtil.trim(statusFilter).toLowerCase(Locale.ROOT);
        List<ApplicationRecord> myApplications = applicationDao.listByApplicant(user.getUserId());

        return currentJobs().stream()
                .filter(job -> matchesKeyword(job, query))
                .filter(job -> matchesSkill(job, skill))
                .filter(job -> matchesStatus(job, status))
                .map(job -> buildJobListing(job, profile, myApplications))
                .sorted(Comparator.comparing(JobListingView::isAvailable).reversed()
                        .thenComparing(JobListingView::getMatchScore, Comparator.reverseOrder())
                        .thenComparing(view -> view.getJob().getDeadline(), Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    public Optional<JobListingView> getJobListing(User user, String jobId) {
        ApplicantProfile profile = applicantDao.findByUserId(user.getUserId()).orElse(null);
        List<ApplicationRecord> myApplications = applicationDao.listByApplicant(user.getUserId());
        return findJob(jobId).map(job -> buildJobListing(job, profile, myApplications));
    }

    public List<JobListingView> recommendedJobs(User user, int limit) {
        return listJobsForApplicant(user, "", "", "open").stream()
                .filter(view -> !view.isApplied())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public ApplicationRecord applyForJob(User user, String jobId, String coverLetter) {
        Job job = findJob(jobId).orElseThrow(() -> new IllegalArgumentException("The selected position could not be found."));
        ApplicantProfile profile = applicantDao.findByUserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Complete your applicant profile before applying."));

        if (!profile.isComplete()) {
            throw new IllegalStateException("Complete your applicant profile before applying.");
        }
        if (!isJobAvailable(job)) {
            throw new IllegalStateException("This position is no longer accepting applications.");
        }
        if (ValidationUtil.trim(coverLetter).length() > 500) {
            throw new IllegalArgumentException("Cover letter must stay within 500 characters.");
        }
        return applicationDao.create(user.getUserId(), jobId, ValidationUtil.trim(coverLetter));
    }

    public List<ApplicationSummaryView> listApplicationsForApplicant(String applicantId) {
        ApplicantProfile profile = applicantDao.findByUserId(applicantId).orElse(null);
        return applicationDao.listByApplicant(applicantId).stream()
                .map(application -> {
                    Job job = findJob(application.getJobId()).orElse(null);
                    if (job == null) {
                        return null;
                    }
                    List<String> skills = profile == null ? new ArrayList<>() : profile.getSkills();
                    return new ApplicationSummaryView(
                            application,
                            job,
                            SkillUtil.baseMatchScore(skills, job.getRequiredSkills()),
                            SkillUtil.matchedSkills(skills, job.getRequiredSkills()),
                            SkillUtil.missingSkills(skills, job.getRequiredSkills())
                    );
                })
                .filter(item -> item != null)
                .collect(Collectors.toList());
    }

    public Map<String, Integer> buildTaMetrics(User user) {
        List<Job> jobs = currentJobs();
        List<ApplicationRecord> applications = applicationDao.listByApplicant(user.getUserId());
        ApplicantProfile profile = applicantDao.findByUserId(user.getUserId()).orElse(null);

        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("openJobs", (int) jobs.stream().filter(this::isJobAvailable).count());
        metrics.put("applications", applications.size());
        metrics.put("accepted", (int) applications.stream().filter(ApplicationRecord::isAccepted).count());
        metrics.put("capacity", profile == null || profile.getMaxWorkloadHoursPerWeek() == null ? 0 : profile.getMaxWorkloadHoursPerWeek());
        return metrics;
    }

    public Map<String, Integer> buildMoMetrics(User user) {
        List<JobOverviewView> jobs = listJobsForMo(user.getUserId());
        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("activeJobs", (int) jobs.stream().filter(view -> !view.getJob().isClosed()).count());
        metrics.put("pendingApplications", jobs.stream().mapToInt(JobOverviewView::getPendingCount).sum());
        metrics.put("acceptedPlacements", jobs.stream().mapToInt(JobOverviewView::getAcceptedCount).sum());
        metrics.put("positionsOpen", jobs.stream().mapToInt(view -> view.getJob().getRemainingQuota()).sum());
        return metrics;
    }

    public Map<String, Integer> buildAdminMetrics() {
        List<WorkloadEntry> workloadEntries = buildWorkloadEntries();
        Map<String, Integer> metrics = new LinkedHashMap<>();
        metrics.put("totalTAs", userDao.listByRole(Role.TA).size());
        metrics.put("overloaded", (int) workloadEntries.stream().filter(WorkloadEntry::isOverload).count());
        metrics.put("openJobs", (int) currentJobs().stream().filter(this::isJobAvailable).count());
        metrics.put("acceptedPlacements", (int) applicationDao.listAll().stream().filter(ApplicationRecord::isAccepted).count());
        return metrics;
    }

    public Job createJob(User mo,
                         String title,
                         String module,
                         String activityType,
                         String description,
                         String requiredSkillsCsv,
                         String quotaValue,
                         String workloadValue,
                         String deadline) {
        List<String> skills = SkillUtil.parseSkills(requiredSkillsCsv);
        int quota = parsePositiveInt(quotaValue, 20, "Enter a valid headcount quota.");
        int workload = parsePositiveInt(workloadValue, 20, "Enter a valid weekly workload.");
        if (ValidationUtil.isBlank(title)) {
            throw new IllegalArgumentException("Job title is required.");
        }
        if (ValidationUtil.isBlank(module)) {
            throw new IllegalArgumentException("Module or activity scope is required.");
        }
        if (ValidationUtil.isBlank(activityType)) {
            throw new IllegalArgumentException("Choose an activity type.");
        }
        if (ValidationUtil.isBlank(description) || ValidationUtil.trim(description).length() < 30) {
            throw new IllegalArgumentException("Job description must contain at least 30 characters.");
        }
        if (skills.isEmpty()) {
            throw new IllegalArgumentException("Add at least one required skill.");
        }
        if (DateTimeUtil.isPastDate(deadline)) {
            throw new IllegalArgumentException("Deadline must be today or later.");
        }

        Job job = new Job();
        job.setMoId(mo.getUserId());
        job.setTitle(ValidationUtil.trim(title));
        job.setModule(ValidationUtil.trim(module));
        job.setActivityType(ValidationUtil.trim(activityType));
        job.setDescription(ValidationUtil.trim(description));
        job.setRequiredSkills(skills);
        job.setQuota(quota);
        job.setWorkloadHoursPerWeek(workload);
        job.setDeadline(ValidationUtil.trim(deadline));
        return jobDao.create(job);
    }

    public List<JobOverviewView> listJobsForMo(String moId) {
        return currentJobs().stream()
                .filter(job -> moId.equals(job.getMoId()))
                .map(this::buildJobOverview)
                .collect(Collectors.toList());
    }

    public List<JobOverviewView> listJobsForAdmin() {
        return currentJobs().stream().map(this::buildJobOverview).collect(Collectors.toList());
    }

    public void closeJobForMo(String moId, String jobId) {
        Job job = findJob(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (!moId.equals(job.getMoId())) {
            throw new IllegalStateException("You can only manage jobs created under your account.");
        }
        job.setStatus("closed");
        jobDao.save(job);
    }

    public void closeJobAsAdmin(String jobId) {
        Job job = findJob(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        job.setStatus("closed");
        jobDao.save(job);
    }

    public List<ApplicantReviewView> listApplicantsForJob(User mo, String jobId, String sort) {
        Job job = findJob(jobId).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (!mo.getUserId().equals(job.getMoId())) {
            throw new IllegalStateException("You can only review applicants for your own positions.");
        }
        List<ApplicantReviewView> applicants = applicationDao.listByJob(jobId).stream()
                .map(application -> buildApplicantReview(job, application))
                .filter(view -> view != null)
                .collect(Collectors.toList());

        if ("skill".equalsIgnoreCase(sort)) {
            applicants.sort(Comparator.comparing(ApplicantReviewView::getMatchScore).reversed());
        } else if ("status".equalsIgnoreCase(sort)) {
            applicants.sort(Comparator.comparing(
                    (ApplicantReviewView view) -> view.getApplication().getStatus(),
                    Comparator.nullsLast(String::compareTo)
            ));
        } else {
            applicants.sort(Comparator.comparing(
                    (ApplicantReviewView view) -> view.getApplication().getAppliedAt(),
                    Comparator.nullsLast(String::compareTo)
            ).reversed());
        }
        return applicants;
    }

    public void reviewApplication(User mo, String applicationId, String decision, String note) {
        ApplicationRecord application = applicationDao.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        Job job = findJob(application.getJobId()).orElseThrow(() -> new IllegalArgumentException("Job not found."));
        if (!mo.getUserId().equals(job.getMoId())) {
            throw new IllegalStateException("You can only review applicants for your own positions.");
        }
        String normalized = ValidationUtil.trim(decision).toLowerCase(Locale.ROOT);
        if (!"accepted".equals(normalized) && !"rejected".equals(normalized)) {
            throw new IllegalArgumentException("Choose either Accept or Reject.");
        }

        int acceptedCount = (int) applicationDao.listByJob(job.getJobId()).stream().filter(ApplicationRecord::isAccepted).count();
        if ("accepted".equals(normalized) && !application.isAccepted() && acceptedCount >= job.getQuota()) {
            job.setStatus("closed");
            jobDao.save(job);
            throw new IllegalStateException("The quota for this position has already been filled.");
        }

        application.setStatus(normalized);
        application.setReviewedAt(DateTimeUtil.nowIso());
        application.setReviewNote(ValidationUtil.isBlank(note) ? null : ValidationUtil.trim(note));
        applicationDao.save(application);
        synchronizeJob(job);
    }

    public List<WorkloadEntry> buildWorkloadEntries() {
        List<User> tas = userDao.listByRole(Role.TA);
        Map<String, List<ApplicationRecord>> acceptedByApplicant = applicationDao.listAll().stream()
                .filter(ApplicationRecord::isAccepted)
                .collect(Collectors.groupingBy(ApplicationRecord::getApplicantId));

        List<WorkloadEntry> entries = new ArrayList<>();
        for (User ta : tas) {
            ApplicantProfile profile = applicantDao.findByUserId(ta.getUserId()).orElse(null);
            List<ApplicationRecord> accepted = acceptedByApplicant.getOrDefault(ta.getUserId(), new ArrayList<>());
            List<String> jobs = new ArrayList<>();
            int totalHours = 0;
            for (ApplicationRecord application : accepted) {
                Job job = findJob(application.getJobId()).orElse(null);
                if (job != null) {
                    totalHours += job.getWorkloadHoursPerWeek();
                    jobs.add(job.getTitle() + " • " + job.getWorkloadHoursPerWeek() + "h/w");
                }
            }

            int maxHours = profile == null || profile.getMaxWorkloadHoursPerWeek() == null ? 0 : profile.getMaxWorkloadHoursPerWeek();
            String loadStatus;
            if (maxHours > 0 && totalHours > maxHours) {
                loadStatus = WorkloadEntry.LOAD_OVERLOAD;
            } else if (totalHours == 0 || (maxHours > 0 && totalHours <= Math.max(1, maxHours / 2))) {
                loadStatus = WorkloadEntry.LOAD_UNDERLOAD;
            } else {
                loadStatus = WorkloadEntry.LOAD_BALANCED;
            }

            WorkloadEntry entry = new WorkloadEntry();
            entry.setUserId(ta.getUserId());
            entry.setName(ta.getName());
            entry.setAcceptedJobs(jobs);
            entry.setTotalHours(totalHours);
            entry.setMaxHours(maxHours);
            entry.setLoadStatus(loadStatus);
            entries.add(entry);
        }

        entries.sort(Comparator.comparing(WorkloadEntry::getLoadStatus)
                .thenComparing(WorkloadEntry::getTotalHours, Comparator.reverseOrder()));
        return entries;
    }

    public List<UserOverviewView> listUsersForAdmin() {
        Map<String, Integer> acceptedHours = buildAcceptedHoursMap();
        return userDao.listAll().stream()
                .sorted(Comparator.comparing(User::getRole).thenComparing(User::getCreatedAt, Comparator.nullsLast(String::compareTo)).reversed())
                .map(user -> {
                    ApplicantProfile profile = applicantDao.findByUserId(user.getUserId()).orElse(null);
                    return new UserOverviewView(
                            user,
                            profile,
                            acceptedHours.getOrDefault(user.getUserId(), 0),
                            profile != null && profile.isComplete(),
                            profile != null && profile.hasCv()
                    );
                })
                .collect(Collectors.toList());
    }

    public void setUserActive(User admin, String targetUserId, boolean active) {
        if (admin.getUserId().equals(targetUserId) && !active) {
            throw new IllegalStateException("You cannot disable the account currently in use.");
        }
        boolean updated = userDao.setActive(targetUserId, active);
        if (!updated) {
            throw new IllegalArgumentException("User not found.");
        }
    }

    private JobListingView buildJobListing(Job job, ApplicantProfile profile, List<ApplicationRecord> myApplications) {
        List<String> applicantSkills = profile == null ? new ArrayList<>() : profile.getSkills();
        boolean profileComplete = profile != null && profile.isComplete();
        boolean applied = myApplications.stream().anyMatch(application -> job.getJobId().equals(application.getJobId()));
        boolean available = isJobAvailable(job);
        String reason = "";
        if (!available) {
            reason = "This position is no longer accepting applications.";
        } else if (!profileComplete) {
            reason = "Complete your applicant profile before applying.";
        } else if (applied) {
            reason = "You have already applied for this position.";
        }
        return new JobListingView(
                job,
                SkillUtil.baseMatchScore(applicantSkills, job.getRequiredSkills()),
                SkillUtil.matchedSkills(applicantSkills, job.getRequiredSkills()),
                SkillUtil.missingSkills(applicantSkills, job.getRequiredSkills()),
                applied,
                profileComplete,
                available,
                reason
        );
    }

    private JobOverviewView buildJobOverview(Job job) {
        List<ApplicationRecord> applications = applicationDao.listByJob(job.getJobId());
        int pending = (int) applications.stream().filter(ApplicationRecord::isPending).count();
        int accepted = (int) applications.stream().filter(ApplicationRecord::isAccepted).count();
        return new JobOverviewView(job, applications.size(), pending, accepted);
    }

    private ApplicantReviewView buildApplicantReview(Job job, ApplicationRecord application) {
        Optional<User> applicantOpt = userDao.findById(application.getApplicantId());
        if (!applicantOpt.isPresent()) {
            return null;
        }
        User applicant = applicantOpt.get();
        ApplicantProfile profile = applicantDao.findByUserId(applicant.getUserId()).orElseGet(() -> {
            ApplicantProfile blank = new ApplicantProfile();
            blank.setUserId(applicant.getUserId());
            blank.setName(applicant.getName());
            blank.setContactEmail(applicant.getEmail());
            return blank;
        });

        List<String> skills = profile.getSkills();
        return new ApplicantReviewView(
                application,
                applicant,
                profile,
                SkillUtil.baseMatchScore(skills, job.getRequiredSkills()),
                SkillUtil.matchedSkills(skills, job.getRequiredSkills()),
                SkillUtil.missingSkills(skills, job.getRequiredSkills()),
                profile.hasCv()
        );
    }

    private List<Job> currentJobs() {
        List<Job> jobs = jobDao.listAll();
        jobs.forEach(this::synchronizeJob);
        return jobs;
    }

    private void synchronizeJob(Job job) {
        List<ApplicationRecord> applications = applicationDao.listByJob(job.getJobId());
        int acceptedCount = (int) applications.stream().filter(ApplicationRecord::isAccepted).count();
        boolean changed = false;

        if (job.getAcceptedCount() != acceptedCount) {
            job.setAcceptedCount(acceptedCount);
            changed = true;
        }
        if (ValidationUtil.isBlank(job.getStatus())) {
            job.setStatus("open");
            changed = true;
        }
        if (acceptedCount >= job.getQuota() || DateTimeUtil.isPastDate(job.getDeadline())) {
            if (!"closed".equalsIgnoreCase(job.getStatus())) {
                job.setStatus("closed");
                changed = true;
            }
        }
        if (changed) {
            jobDao.save(job);
        }
    }

    private boolean isJobAvailable(Job job) {
        return !"closed".equalsIgnoreCase(job.getStatus())
                && !DateTimeUtil.isPastDate(job.getDeadline())
                && job.getAcceptedCount() < job.getQuota();
    }

    private boolean matchesKeyword(Job job, String query) {
        if (ValidationUtil.isBlank(query)) {
            return true;
        }
        String haystack = String.join(" ",
                ValidationUtil.trim(job.getTitle()),
                ValidationUtil.trim(job.getModule()),
                ValidationUtil.trim(job.getActivityType()),
                ValidationUtil.trim(job.getDescription())
        ).toLowerCase(Locale.ROOT);
        return haystack.contains(query);
    }

    private boolean matchesSkill(Job job, String skill) {
        if (ValidationUtil.isBlank(skill)) {
            return true;
        }
        return job.getRequiredSkills().stream().anyMatch(item -> item.toLowerCase(Locale.ROOT).contains(skill));
    }

    private boolean matchesStatus(Job job, String status) {
        if (ValidationUtil.isBlank(status) || "all".equals(status)) {
            return true;
        }
        if ("open".equals(status)) {
            return isJobAvailable(job);
        }
        if ("closed".equals(status)) {
            return !isJobAvailable(job);
        }
        return true;
    }

    private Map<String, Integer> buildAcceptedHoursMap() {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ApplicationRecord application : applicationDao.listAll()) {
            if (!application.isAccepted()) {
                continue;
            }
            Job job = findJob(application.getJobId()).orElse(null);
            if (job == null) {
                continue;
            }
            result.merge(application.getApplicantId(), job.getWorkloadHoursPerWeek(), Integer::sum);
        }
        return result;
    }

    private int parsePositiveInt(String rawValue, int upperBound, String errorMessage) {
        try {
            int value = Integer.parseInt(ValidationUtil.trim(rawValue));
            if (value <= 0 || value > upperBound) {
                throw new IllegalArgumentException(errorMessage);
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
