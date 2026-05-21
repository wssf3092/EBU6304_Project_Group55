package com.group55.ta.controller;

import com.group55.ta.dto.JobOverviewView;
import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * MO dashboard overview.
 */
@WebServlet("/mo/dashboard")
public class MODashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        List<JobOverviewView> jobs = recruitmentService.listJobsForMo(user.getUserId());
        request.setAttribute("metrics", recruitmentService.buildMoMetrics(user));
        request.setAttribute("jobs", jobs);
        request.setAttribute("statusChartJson", buildStatusChartJson(jobs));
        render(request, response, "mo/dashboard.jsp", "MO Dashboard", "Monitor pipeline health and move applications through review.", "mo-dashboard");
    }

    private String buildStatusChartJson(List<JobOverviewView> jobs) {
        int pending = jobs.stream().mapToInt(JobOverviewView::getPendingCount).sum();
        int accepted = jobs.stream().mapToInt(JobOverviewView::getAcceptedCount).sum();
        int total = jobs.stream().mapToInt(JobOverviewView::getApplicationCount).sum();
        int rejected = Math.max(0, total - pending - accepted);
        return "{\"pending\":" + pending + ",\"accepted\":" + accepted + ",\"rejected\":" + rejected + "}";
    }
}
