package com.group55.ta.controller;

import com.group55.ta.model.Course;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TA browses all courses (Step 4: replaces legacy {@code /courses}).
 */
@WebServlet("/ta/courses")
public class TACoursesServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("rolePrefix", "/ta");
        List<Course> courses = recruitmentService.listAllCoursesEnriched();
        request.setAttribute("courses", courses);
        forwardToView(request, response, "course-list");
    }
}
