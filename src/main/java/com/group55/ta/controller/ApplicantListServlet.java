package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Applicant review page for a specific job.
 */
@WebServlet("/mo/jobs/applicants")
public class ApplicantListServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String jobId = request.getParameter("jobId");
        String sort = request.getParameter("sort");
        try {
            request.setAttribute("job", recruitmentService.findJob(jobId).orElse(null));
            request.setAttribute("sort", sort == null || sort.trim().isEmpty() ? "recent" : sort);
            request.setAttribute("applicants", recruitmentService.listApplicantsForJob(user, jobId, sort));
            render(request, response, "mo/applicants.jsp", "Review Applicants", "Compare evidence, inspect fit, and record final decisions.", "mo-applicants");
        } catch (Exception ex) {
            request.setAttribute("formError", ex.getMessage());
            request.setAttribute("jobs", recruitmentService.listJobsForMo(user.getUserId()));
            request.setAttribute("skillCatalog", recruitmentService.getSkillCatalog());
            request.setAttribute("activityTypes", recruitmentService.getActivityTypes());
            render(request, response, "mo/jobs.jsp", "Manage Jobs", "Create openings, monitor demand, and close positions when needed.", "mo-jobs");
        }
    }
}
