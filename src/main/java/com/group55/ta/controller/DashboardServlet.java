package com.group55.ta.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class DashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object currentUser = session == null ? null : session.getAttribute("currentUser");
        request.setAttribute("currentUser", currentUser);

        if (currentUser != null) {
            String role = getUserRoleName(currentUser);
            if ("STUDENT".equalsIgnoreCase(role)) {
                request.setAttribute("applications", safeFindStudentApplications(currentUser));
            } else if ("TEACHER".equalsIgnoreCase(role)) {
                request.setAttribute("courses", safeFindAllCourses());
            } else if ("ADMIN".equalsIgnoreCase(role)) {
                request.setAttribute("courses", safeFindAllCourses());
            }
        }

        forwardToView(request, response, "dashboard");
    }

    private String getUserRoleName(Object user) {
        try {
            Method getRole = user.getClass().getMethod("getRole");
            Object role = getRole.invoke(user);
            return role == null ? null : String.valueOf(role);
        } catch (Exception e) {
            return null;
        }
    }

    private String getUsername(Object user) {
        try {
            Method getUsername = user.getClass().getMethod("getUsername");
            Object v = getUsername.invoke(user);
            return v == null ? null : String.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> safeFindStudentApplications(Object user) throws ServletException {
        String username = getUsername(user);
        if (username == null) {
            return Collections.emptyList();
        }
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.ApplicationDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method findByStudentUsername = daoClass.getMethod("findByStudentUsername", String.class);
            Object result = findByStudentUsername.invoke(dao, username);
            return result == null ? Collections.emptyList() : (List<Object>) result;
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ServletException("加载学生申请失败", e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Object> safeFindAllCourses() throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.CourseDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method findAll = daoClass.getMethod("findAll");
            Object result = findAll.invoke(dao);
            return result == null ? Collections.emptyList() : (List<Object>) result;
        } catch (ClassNotFoundException e) {
            return Collections.emptyList();
        } catch (Exception e) {
            throw new ServletException("加载课程列表失败", e);
        }
    }
}

