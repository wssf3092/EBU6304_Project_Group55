package com.group55.ta.controller;

import com.group55.ta.model.Course;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/mo/dashboard")
public class MODashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        List<Course> courses = recruitmentService.listCoursesEnrichedForMo(user.getUserId());
        request.setAttribute("courses", courses);
        request.setAttribute("pageTitle", "仪表盘");
        request.setAttribute("activeNav", "mo-dashboard");
        forwardToView(request, response, "mo/dashboard");
    }
}
