package com.group55.ta.controller;

import com.group55.ta.dao.ApplicationDao;
import com.group55.ta.dao.CourseDao;
import com.group55.ta.model.Application;
import com.group55.ta.model.Course;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CourseManageServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("id"));
        if (courseId == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        CourseDao courseDao = new CourseDao();
        Course course = courseDao.findById(courseId);
        if (course == null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        if (!isTeacherOwner(user, course)) {
            session.setAttribute("errorMessage", "无权限管理该课程");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        ApplicationDao appDao = new ApplicationDao();
        List<Application> applications = appDao.findByCourseId(courseId);
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
        User user = session == null ? null : (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String applicationId = trimToNull(request.getParameter("applicationId"));
        String action = trimToNull(request.getParameter("action"));
        if (courseId == null || applicationId == null || action == null) {
            session.setAttribute("errorMessage", "参数不完整，无法更新申请状态");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        CourseDao courseDao = new CourseDao();
        Course course = courseDao.findById(courseId);
        if (course == null || !isTeacherOwner(user, course)) {
            session.setAttribute("errorMessage", "无权限管理该课程");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        ApplicationDao appDao = new ApplicationDao();
        if ("approve".equalsIgnoreCase(action)) {
            int approvedCount = countApproved(appDao.findByCourseId(courseId));
            if (approvedCount >= course.getTaNeedCount()) {
                session.setAttribute("errorMessage", "该课程 TA 名额已满，无法继续通过");
                response.sendRedirect(request.getContextPath() + "/courses/manage?id=" + courseId);
                return;
            }
            appDao.updateStatus(applicationId, Application.Status.APPROVED);
            session.setAttribute("successMessage", "申请已通过");
        } else if ("reject".equalsIgnoreCase(action)) {
            appDao.updateStatus(applicationId, Application.Status.REJECTED);
            session.setAttribute("successMessage", "申请已拒绝");
        } else {
            session.setAttribute("errorMessage", "未知操作类型");
        }

        response.sendRedirect(request.getContextPath() + "/courses/manage?id=" + courseId);
    }

    private boolean isTeacherOwner(User user, Course course) {
        if (user == null || course == null) {
            return false;
        }
        String role = user.getRole();
        boolean isTeacher = "TEACHER".equalsIgnoreCase(role) || "Teacher".equals(role);
        return isTeacher && user.getUsername() != null && user.getUsername().equals(course.getTeacherUsername());
    }

    private int countApproved(List<Application> apps) {
        int count = 0;
        if (apps == null) {
            return 0;
        }
        for (Application app : apps) {
            if (app != null && app.getStatus() == Application.Status.APPROVED) {
                count++;
            }
        }
        return count;
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

