package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MO job management page.
 */
@WebServlet("/mo/jobs")
public class MOJobServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("jobs", recruitmentService.listJobsForMo(user.getUserId()));
        request.setAttribute("skillCatalog", recruitmentService.getSkillCatalog());
        request.setAttribute("activityTypes", recruitmentService.getActivityTypes());
        render(request, response, "mo/jobs.jsp", "Manage Jobs", "Create openings, monitor demand, and close positions when needed.", "mo-jobs");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String action = request.getParameter("action");
        try {
            if ("close".equalsIgnoreCase(action)) {
                recruitmentService.closeJobForMo(user.getUserId(), request.getParameter("jobId"));
                FlashUtil.success(request, "Position closed.");
                redirect(request, response, "/mo/jobs");
                return;
            }
            recruitmentService.createJob(
                    user,
                    request.getParameter("title"),
                    request.getParameter("module"),
                    request.getParameter("activityType"),
                    request.getParameter("description"),
                    request.getParameter("requiredSkills"),
                    request.getParameter("quota"),
                    request.getParameter("workload"),
                    request.getParameter("deadline")
            );
            FlashUtil.success(request, "Position created.");
            redirect(request, response, "/mo/jobs");
        } catch (Exception ex) {
            request.setAttribute("formError", ex.getMessage());
            doGet(request, response);
        }
    }
}
