package com.group55.ta.controller;

import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/mo/courses/manage")
public class CourseManageServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
            FlashUtil.error(request, "无权限管理该课程");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        List<Application> applications = recruitmentService.listApplicationsForCourse(courseId);
        request.setAttribute("course", course);
        request.setAttribute("applications", applications);
        request.setAttribute("pageTitle", "管理申请 · " + course.getName());
        request.setAttribute("activeNav", "mo-dashboard");
        forwardToView(request, response, "mo/courses");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/auth/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String applicationId = trimToNull(request.getParameter("applicationId"));
        String action = trimToNull(request.getParameter("action"));
        if (courseId == null || applicationId == null || action == null) {
            FlashUtil.error(request, "参数不完整，无法更新申请状态");
            response.sendRedirect(request.getContextPath() + "/mo/dashboard");
            return;
        }

        Course course = recruitmentService.findCourse(courseId);
        if (course == null || !isMoOwner(user, course)) {
            FlashUtil.error(request, "无权限管理该课程");
            response.sendRedirect(request.getContextPath() + "/mo/dashboard");
            return;
        }

        if ("accept".equalsIgnoreCase(action)) {
            try {
                recruitmentService.reviewCourseApplication(user, courseId, applicationId, true);
                FlashUtil.success(request, "申请已通过");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                FlashUtil.error(request, ex.getMessage());
            }
        } else if ("reject".equalsIgnoreCase(action)) {
            try {
                recruitmentService.reviewCourseApplication(user, courseId, applicationId, false);
                FlashUtil.success(request, "申请已拒绝");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                FlashUtil.error(request, ex.getMessage());
            }
        } else {
            FlashUtil.error(request, "未知操作类型");
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
