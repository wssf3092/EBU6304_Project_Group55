package com.group55.ta.controller;

import com.group55.ta.dao.CourseDao;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/mo/courses/new")
public class CourseCreateServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "发布课程");
        request.setAttribute("activeNav", "mo-course-new");
        forwardToView(request, response, "mo/course-new");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "发布课程");
        request.setAttribute("activeNav", "mo-course-new");
        User user = currentUser(request);
        if (user == null) {
            redirect(request, response, "/auth/login");
            return;
        }
        if (user.getRoleEnum() != Role.MO) {
            redirect(request, response, homePathFor(user));
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
            forwardToView(request, response, "mo/course-new");
            return;
        }

        int taNeedCount;
        try {
            taNeedCount = Integer.parseInt(taNeedCountRaw);
            if (taNeedCount < 1) {
                request.setAttribute("errorMessage", "TA 人数必须为正整数");
                forwardToView(request, response, "mo/course-new");
                return;
            }
        } catch (NumberFormatException e) {
            request.setAttribute("errorMessage", "TA 人数必须为正整数");
            forwardToView(request, response, "mo/course-new");
            return;
        }

        Course course = new Course();
        course.setCourseId(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        course.setName(courseName);
        course.setTeacher(user.getUserId());
        course.setDescription(description == null ? "" : description);
        course.setTaNeedCount(taNeedCount);
        course.setCurrentTaCount(0);

        new CourseDao().save(course);
        FlashUtil.success(request, "课程已发布");
        redirect(request, response, "/mo/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
