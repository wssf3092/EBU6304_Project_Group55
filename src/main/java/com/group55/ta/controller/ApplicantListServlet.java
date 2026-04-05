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

/**
 * MO 查看某课程的申请人列表（Step 6）。
 */
@WebServlet("/mo/courses/applicants")
public class ApplicantListServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String courseId = trimToNull(request.getParameter("courseId"));
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
            request.setAttribute("formError", "无权限查看该课程的申请。");
            request.setAttribute("pageTitle", "仪表盘");
            request.setAttribute("activeNav", "mo-dashboard");
            request.setAttribute("courses", recruitmentService.listCoursesEnrichedForMo(user.getUserId()));
            forwardToView(request, response, "mo/dashboard");
            return;
        }

        List<Application> applications = recruitmentService.listApplicationsEnrichedForCourseMoView(courseId);
        request.setAttribute("course", course);
        request.setAttribute("applications", applications);
        request.setAttribute("pageTitle", "申请人 · " + course.getName());
        request.setAttribute("activeNav", "mo-dashboard");
        forwardToView(request, response, "mo/applicants");
    }

    private boolean isMoOwner(User u, Course course) {
        if (u == null || course == null) {
            return false;
        }
        return u.getRoleEnum() == Role.MO && u.getUserId() != null && u.getUserId().equals(course.getTeacher());
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
