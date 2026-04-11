package com.group55.ta.controller;

import com.group55.ta.model.Course;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Course> courses = recruitmentService.listAllCoursesEnrichedForAdmin();
        request.setAttribute("courses", courses);
        request.setAttribute("pageTitle", "仪表盘");
        request.setAttribute("activeNav", "admin-dashboard");
        forwardToView(request, response, "admin/dashboard");
    }
}
