package com.group55.ta.controller;

import com.group55.ta.model.Course;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * MO 课程管理入口：跳转到申请人列表（Step 6；兼容原 /mo/courses/manage）。
 */
@WebServlet(name = "MOCourseServlet", urlPatterns = {"/mo/courses/manage", "/mo/courses"})
public class MOCourseServlet extends BaseServlet {

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

        String enc = URLEncoder.encode(courseId, StandardCharsets.UTF_8.name());
        redirect(request, response, "/mo/courses/applicants?courseId=" + enc);
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
