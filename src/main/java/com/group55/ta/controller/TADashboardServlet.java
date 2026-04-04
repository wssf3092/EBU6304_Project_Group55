package com.group55.ta.controller;

import com.group55.ta.model.Application;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ta/dashboard")
public class TADashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        List<Application> apps = recruitmentService.listApplicationsEnrichedForApplicant(user.getUserId());

        int pending = 0;
        int accepted = 0;
        int rejected = 0;
        for (Application app : apps) {
            Application.Status s = app.getStatusEnum();
            if (s == Application.Status.PENDING) {
                pending++;
            } else if (s == Application.Status.ACCEPTED) {
                accepted++;
            } else if (s == Application.Status.REJECTED) {
                rejected++;
            }
        }
        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("total", apps.size());
        metrics.put("pending", pending);
        metrics.put("accepted", accepted);
        metrics.put("rejected", rejected);
        request.setAttribute("metrics", metrics);

        List<Application> recent = apps.size() > 5 ? new ArrayList<>(apps.subList(0, 5)) : apps;
        request.setAttribute("recentApplications", recent);
        request.setAttribute("pageTitle", "仪表盘");
        request.setAttribute("activeNav", "ta-dashboard");
        forwardToView(request, response, "ta/dashboard");
    }
}
