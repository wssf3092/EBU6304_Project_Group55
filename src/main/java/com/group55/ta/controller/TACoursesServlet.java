package com.group55.ta.controller;

import com.group55.ta.model.Course;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ta/courses")
public class TACoursesServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Course> courses = recruitmentService.listAllCoursesEnriched();
        request.setAttribute("courses", courses);
        request.setAttribute("pageTitle", "浏览课程");
        request.setAttribute("activeNav", "ta-courses");
        forwardToView(request, response, "ta/courses");
    }
}
