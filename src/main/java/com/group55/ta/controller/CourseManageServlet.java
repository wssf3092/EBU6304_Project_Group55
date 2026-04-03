package com.group55.ta.controller;

import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/mo/courses/manage")
public class CourseManageServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = currentUser(request);
        if (user == null) {
            redirect(request, response, "/auth/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("id"));
        if (courseId == null) {
            redirect(request, response, "/mo/dashboard");
            return;
        }

        Course course = recruitmentService.findCourse(courseId);
        if (course == null) {
            redirect(request, response, "/mo/dashboard");
            return;
        }

        if (!isMoOwner(user, course)) {
            session.setAttribute("errorMessage", "无权限管理该课程");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        request.setAttribute("rolePrefix", "/mo");
        List<Application> applications = recruitmentService.listApplicationsForCourse(courseId);
        request.setAttribute("course", course);
        request.setAttribute("applications", applications);

        Object flashError = session.getAttribute("errorMessage");
        if (flashError != null) {
            request.setAttribute("errorMessage", flashError);
            session.removeAttribute("errorMessage");
        }
        Object flashSuccess = session.getAttribute("successMessage");
        if (flashSuccess != null) {
            request.setAttribute("successMessage", flashSuccess);
            session.removeAttribute("successMessage");
        }

        forwardToView(request, response, "course-manage");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = currentUser(request);
        if (user == null) {
            redirect(request, response, "/auth/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String applicationId = trimToNull(request.getParameter("applicationId"));
        String action = trimToNull(request.getParameter("action"));
        if (courseId == null || applicationId == null || action == null) {
            session.setAttribute("errorMessage", "参数不完整，无法更新申请状态");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        Course course = recruitmentService.findCourse(courseId);
        if (course == null || !isMoOwner(user, course)) {
            session.setAttribute("errorMessage", "无权限管理该课程");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        if ("accept".equalsIgnoreCase(action)) {
            try {
                recruitmentService.reviewCourseApplication(user, courseId, applicationId, true);
                session.setAttribute("successMessage", "申请已通过");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                session.setAttribute("errorMessage", ex.getMessage());
            }
        } else if ("reject".equalsIgnoreCase(action)) {
            try {
                recruitmentService.reviewCourseApplication(user, courseId, applicationId, false);
                session.setAttribute("successMessage", "申请已拒绝");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                session.setAttribute("errorMessage", ex.getMessage());
            }
        } else {
            session.setAttribute("errorMessage", "未知操作类型");
        }

        response.sendRedirect(request.getContextPath() + "/mo/courses/manage?id=" + courseId);
    }

    private boolean isMoOwner(User user, Course course) {
        if (user == null || course == null) {
            return false;
        }
        return user.getRoleEnum() == Role.MO
                && user.getUserId() != null
                && user.getUserId().equals(course.getTeacher());
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
