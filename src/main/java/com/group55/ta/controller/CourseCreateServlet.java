package com.group55.ta.controller;

import com.group55.ta.dao.CourseDao;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CourseCreateServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "course-create");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Role roleEnum = user.getRoleEnum();
        if (roleEnum != Role.MO && !"MO".equalsIgnoreCase(user.getRole())
                && !"TEACHER".equalsIgnoreCase(user.getRole()) && !"Teacher".equals(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String courseName = trimToNull(request.getParameter("courseName"));
        String taNeedCountRaw = trimToNull(request.getParameter("taNeedCount"));
        String description = trimToNull(request.getParameter("description"));

        request.setAttribute("courseName", courseName);
        request.setAttribute("taNeedCount", taNeedCountRaw);
        request.setAttribute("description", description == null ? "" : description);

        if (courseName == null || taNeedCountRaw == null) {
            request.setAttribute("errorMessage", "课程名称和 TA 人数为必填项");
            forwardToView(request, response, "course-create");
            return;
        }

        int taNeedCount;
        try {
            taNeedCount = Integer.parseInt(taNeedCountRaw);
            if (taNeedCount < 1) {
                request.setAttribute("errorMessage", "TA 人数必须为正整数");
                forwardToView(request, response, "course-create");
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "TA 人数必须为正整数");
            forwardToView(request, response, "course-create");
            return;
        }

        Course course = new Course();
        course.setCourseId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        course.setName(courseName);
        course.setTeacher(user.getUserId());
        course.setDescription(description == null ? "" : description);
        course.setTaNeedCount(taNeedCount);
        course.setCurrentTaCount(0);

        CourseDao dao = new CourseDao();
        dao.save(course);

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
