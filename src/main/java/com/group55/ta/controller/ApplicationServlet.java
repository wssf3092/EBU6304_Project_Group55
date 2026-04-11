package com.group55.ta.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.model.Course;
import com.group55.ta.model.User;

public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String courseId = trimToNull(request.getParameter("courseId"));
        if (courseId != null) {
            CourseDao courseDao = new CourseDao();
            Course course = courseDao.findById(courseId);
            request.setAttribute("course", course);
            request.setAttribute("targetCourse", course);
            request.setAttribute("courseId", courseId);
        }
        forwardToView(request, response, "apply");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String statement = trimToNull(request.getParameter("statement"));
        if (courseId == null || statement == null) {
            request.setAttribute("errorMessage", "课程与申请陈述不能为空");
            request.setAttribute("courseId", courseId);
            CourseDao courseDao = new CourseDao();
            Course course = courseDao.findById(courseId);
            request.setAttribute("targetCourse", course);
            forwardToView(request, response, "apply");
            return;
        }

        try {
            ApplicationDao dao = new ApplicationDao();
            dao.create(user.getUserId(), courseId, statement);
        } catch (IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            CourseDao courseDao = new CourseDao();
            request.setAttribute("targetCourse", courseDao.findById(courseId));
            forwardToView(request, response, "apply");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
