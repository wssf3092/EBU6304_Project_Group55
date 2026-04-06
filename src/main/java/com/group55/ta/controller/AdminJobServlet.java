package com.group55.ta.controller;

import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin job management page.
 */
@WebServlet("/admin/jobs")
public class AdminJobServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("jobs", recruitmentService.listJobsForAdmin());
        render(request, response, "admin/jobs.jsp", "Job Management", "Inspect application flow and close positions when required.", "admin-jobs");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            recruitmentService.closeJobAsAdmin(request.getParameter("jobId"));
            FlashUtil.success(request, "Position closed.");
        } catch (Exception ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirect(request, response, "/admin/jobs");
    }
}
