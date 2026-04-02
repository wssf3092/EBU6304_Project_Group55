package com.group55.ta.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.group55.ta.model.Course;
import com.group55.ta.service.RecruitmentService;

public class CourseListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        RecruitmentService recruitmentService = new RecruitmentService();
        List<Course> courses = recruitmentService.listAllCoursesEnriched();
        request.setAttribute("courses", courses);
        forwardToView(request, response, "course-list");
    }
}
