package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MO dashboard overview.
 */
@WebServlet("/mo/dashboard")
public class MODashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("metrics", recruitmentService.buildMoMetrics(user));
        request.setAttribute("jobs", recruitmentService.listJobsForMo(user.getUserId()));
        render(request, response, "mo/dashboard.jsp", "MO Dashboard", "Monitor pipeline health and move applications through review.", "mo-dashboard");
    }
}
