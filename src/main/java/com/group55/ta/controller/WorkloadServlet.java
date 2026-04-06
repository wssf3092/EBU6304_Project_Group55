package com.group55.ta.controller;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin workload overview page.
 */
@WebServlet("/admin/workload")
public class WorkloadServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("metrics", recruitmentService.buildAdminMetrics());
        request.setAttribute("workloads", recruitmentService.buildWorkloadEntries());
        request.setAttribute("jobs", recruitmentService.listJobsForAdmin());
        render(request, response, "admin/workload.jsp", "Workload Overview", "Track placement pressure and spot rebalancing opportunities.", "admin-workload");
    }
}
