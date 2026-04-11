package com.group55.ta.controller;

import com.group55.ta.dto.ApplicationSummaryView;
import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * TA dashboard overview.
 */
@WebServlet("/ta/dashboard")
public class TADashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        ApplicantProfile profile = recruitmentService.findProfile(user.getUserId()).orElse(recruitmentService.getOrCreateProfile(user));
        List<ApplicationSummaryView> applications = recruitmentService.listApplicationsForApplicant(user.getUserId());
        request.setAttribute("profile", profile);
        request.setAttribute("metrics", recruitmentService.buildTaMetrics(user));
        request.setAttribute("recommendedJobs", recruitmentService.recommendedJobs(user, 4));
        request.setAttribute("recentApplications", applications.stream().limit(5).toArray());
        render(request, response, "ta/dashboard.jsp", "TA Dashboard", "Track profile readiness, opportunities, and application progress.", "ta-dashboard");
    }
}
