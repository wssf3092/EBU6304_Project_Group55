package com.group55.ta.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Course;
import com.group55.ta.util.EntityHydrator;

public class CourseListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        CourseDao courseDao = new CourseDao();
        UserDao userDao = new UserDao();
        ApplicationDao applicationDao = new ApplicationDao();
        List<Course> courses = courseDao.findAll();
        EntityHydrator.enrichCourses(courses, userDao, applicationDao);
        request.setAttribute("courses", courses);
        forwardToView(request, response, "course-list");
    }
}
