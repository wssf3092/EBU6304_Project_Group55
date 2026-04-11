package com.group55.ta.controller;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.EntityHydrator;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        request.setAttribute("currentUser", user);

        if (user != null) {
            Role roleEnum = user.getRoleEnum();
            String role = user.getRole();
            UserDao userDao = new UserDao();
            CourseDao courseDao = new CourseDao();
            ApplicationDao applicationDao = new ApplicationDao();

            if (roleEnum == Role.TA || "TA".equalsIgnoreCase(role) || "Student".equalsIgnoreCase(role)) {
                List<Application> apps = applicationDao.findByApplicant(user.getUserId());
                EntityHydrator.enrichApplications(apps, courseDao, userDao);
                request.setAttribute("applications", apps);
            } else if (roleEnum == Role.MO || "MO".equalsIgnoreCase(role) || "TEACHER".equalsIgnoreCase(role) || "Teacher".equals(role)) {
                List<Course> courses = courseDao.findByTeacher(user.getUserId());
                EntityHydrator.enrichCourses(courses, userDao, applicationDao);
                request.setAttribute("courses", courses);
            } else if (roleEnum == Role.ADMIN || "ADMIN".equalsIgnoreCase(role) || "Admin".equals(role)) {
                List<Course> courses = courseDao.findAll();
                EntityHydrator.enrichCourses(courses, userDao, applicationDao);
                request.setAttribute("courses", courses);
            }
        }

        forwardToView(request, response, "dashboard");
    }
}
