package com.group55.ta.controller;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.Collections;
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
            String role = user.getRole();
            if ("STUDENT".equalsIgnoreCase(role)) {
                request.setAttribute("applications", safeFindStudentApplications(user.getUsername()));
            } else if ("TEACHER".equalsIgnoreCase(role)) {
                String teacherUsername = user.getUsername();
                request.setAttribute("courses", safeFindCoursesByTeacher(teacherUsername));
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                request.setAttribute("courses", safeFindAllCourses());
            }
        }

        forwardToView(request, response, "dashboard");
    }

    private List<Application> safeFindStudentApplications(String username) throws ServletException {
        if (username == null) {
            return Collections.emptyList();
        }
        try {
            ApplicationDao dao = new ApplicationDao();
            List<Application> result = dao.findByStudentUsername(username);
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            throw new ServletException("加载学生申请失败", e);
        }
    }

    private List<Course> safeFindAllCourses() throws ServletException {
        try {
            CourseDao dao = new CourseDao();
            List<Course> result = dao.findAll();
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            throw new ServletException("加载课程列表失败", e);
        }
    }

    private List<Course> safeFindCoursesByTeacher(String username) throws ServletException {
        if (username == null) {
            return Collections.emptyList();
        }
        try {
            CourseDao dao = new CourseDao();
            List<Course> result = dao.findByTeacher(username);
            return result == null ? Collections.emptyList() : result;
        } catch (Exception e) {
            throw new ServletException("加载教师课程列表失败", e);
        }
    }
}

