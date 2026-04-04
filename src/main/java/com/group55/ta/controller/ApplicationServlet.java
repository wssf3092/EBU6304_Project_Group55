package com.group55.ta.controller;

import com.group55.ta.model.Course;
import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/ta/courses/apply")
public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "申请 TA");
        request.setAttribute("activeNav", "ta-courses");
        String courseId = trimToNull(request.getParameter("courseId"));
        if (courseId != null) {
            Course course = recruitmentService.findCourse(courseId);
            request.setAttribute("course", course);
            request.setAttribute("targetCourse", course);
            request.setAttribute("courseId", courseId);
        }
        forwardToView(request, response, "ta/apply");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "申请 TA");
        request.setAttribute("activeNav", "ta-courses");
        User user = currentUser(request);
        if (user == null) {
            redirect(request, response, "/auth/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String statement = trimToNull(request.getParameter("statement"));
        if (courseId == null || statement == null) {
            request.setAttribute("errorMessage", "课程与申请陈述不能为空");
            request.setAttribute("courseId", courseId);
            request.setAttribute("targetCourse", recruitmentService.findCourse(courseId));
            forwardToView(request, response, "ta/apply");
            return;
        }

        try {
            recruitmentService.applyForCourse(user.getUserId(), courseId, statement);
        } catch (IllegalArgumentException | IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("targetCourse", recruitmentService.findCourse(courseId));
            forwardToView(request, response, "ta/apply");
            return;
        }

        FlashUtil.success(request, "申请已提交");
        redirect(request, response, "/ta/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
