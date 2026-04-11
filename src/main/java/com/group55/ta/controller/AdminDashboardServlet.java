package com.group55.ta.controller;

import com.group55.ta.model.Course;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin workspace (all courses, read-only overview).
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("rolePrefix", "/admin");
        request.setAttribute("dashboardMode", "admin");
        List<Course> courses = recruitmentService.listAllCoursesEnrichedForAdmin();
        request.setAttribute("courses", courses);
        forwardToView(request, response, "dashboard");
    }
}
