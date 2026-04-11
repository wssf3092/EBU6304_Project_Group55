package com.group55.ta.controller;

import com.group55.ta.model.Course;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MO workspace home (courses they own).
 */
@WebServlet("/mo/dashboard")
public class MODashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("rolePrefix", "/mo");
        request.setAttribute("dashboardMode", "mo");
        List<Course> courses = recruitmentService.listCoursesEnrichedForMo(user.getUserId());
        request.setAttribute("courses", courses);
        forwardToView(request, response, "dashboard");
    }
}
