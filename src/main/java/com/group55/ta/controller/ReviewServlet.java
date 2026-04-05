package com.group55.ta.controller;

import com.group55.ta.model.Application;
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
 * MO 审核单条申请（Step 6）。
 */
@WebServlet("/mo/applications/review")
public class ReviewServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String applicationId = trimToNull(request.getParameter("id"));
        if (applicationId == null) {
            redirect(request, response, "/mo/dashboard");
            return;
        }

        Application app = recruitmentService.getApplicationForMoReview(user, applicationId);
        if (app == null) {
            FlashUtil.error(request, "无法加载申请或无权审核。");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        Course course = recruitmentService.findCourse(app.getCourseId());
        User applicant = recruitmentService.findApplicantUser(app.getApplicantId());

        request.setAttribute("application", app);
        request.setAttribute("course", course);
        request.setAttribute("applicant", applicant);
        request.setAttribute("pageTitle", "审核申请");
        request.setAttribute("activeNav", "mo-dashboard");
        forwardToView(request, response, "mo/review");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String applicationId = trimToNull(request.getParameter("applicationId"));
        String courseId = trimToNull(request.getParameter("courseId"));
        String action = trimToNull(request.getParameter("action"));
        String note = request.getParameter("note");

        if (applicationId == null || courseId == null || action == null) {
            FlashUtil.error(request, "参数不完整。");
            redirect(request, response, "/mo/dashboard");
            return;
        }

        boolean accept = "accept".equalsIgnoreCase(action);
        if (!accept && !"reject".equalsIgnoreCase(action)) {
            FlashUtil.error(request, "未知操作。");
            redirectBack(response, request.getContextPath(), courseId);
            return;
        }

        try {
            recruitmentService.reviewApplication(user, applicationId, accept, note);
            FlashUtil.success(request, "审核已保存。");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirectBack(response, request.getContextPath(), courseId);
    }

    private void redirectBack(HttpServletResponse response, String ctx, String courseId) throws IOException {
        String enc = URLEncoder.encode(courseId, StandardCharsets.UTF_8.name());
        response.sendRedirect(ctx + "/mo/courses/applicants?courseId=" + enc);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
