package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TA job browser and detail page.
 */
@WebServlet("/ta/jobs")
public class JobBrowseServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String query = request.getParameter("q");
        String skill = request.getParameter("skill");
        String status = request.getParameter("status");
        String jobId = request.getParameter("jobId");

        request.setAttribute("query", query);
        request.setAttribute("skill", skill);
        request.setAttribute("status", status == null || status.trim().isEmpty() ? "open" : status);
        request.setAttribute("jobs", recruitmentService.listJobsForApplicant(user, query, skill, status == null ? "open" : status));
        request.setAttribute("selectedJob", recruitmentService.getJobListing(user, jobId).orElse(null));
        request.setAttribute("profile", recruitmentService.findProfile(user.getUserId()).orElse(recruitmentService.getOrCreateProfile(user)));
        request.setAttribute("skillCatalog", recruitmentService.getSkillCatalog());
        render(request, response, "ta/jobs.jsp", "Available Jobs", "Search positions, inspect fit, and submit targeted applications.", "ta-jobs");
    }
}
