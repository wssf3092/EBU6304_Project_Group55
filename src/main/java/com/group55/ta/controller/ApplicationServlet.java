package com.group55.ta.controller;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class ApplicationServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String courseId = trimToNull(request.getParameter("courseId"));
        if (courseId != null) {
            request.setAttribute("course", safeFindCourseById(courseId));
            request.setAttribute("courseId", courseId);
        }
        forwardToView(request, response, "apply");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object currentUser = session == null ? null : session.getAttribute("user");
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String courseId = trimToNull(request.getParameter("courseId"));
        String statement = trimToNull(request.getParameter("statement"));
        if (courseId == null || statement == null) {
            request.setAttribute("errorMessage", "课程与申请陈述不能为空");
            request.setAttribute("courseId", courseId);
            forwardToView(request, response, "apply");
            return;
        }

        saveApplication(currentUser, courseId, statement);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
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

    private Object safeFindCourseById(String courseId) throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.CourseDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method findById = daoClass.getMethod("findById", String.class);
            return findById.invoke(dao, courseId);
        } catch (ClassNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new ServletException("加载课程信息失败", e);
        }
    }

    private void saveApplication(Object currentUser, String courseId, String statement) throws ServletException {
        String username = getUsername(currentUser);
        if (username == null) {
            throw new ServletException("无法获取当前用户用户名");
        }

        try {
            Class<?> appClass = Class.forName("com.group55.ta.model.Application");
            Object app = appClass.getDeclaredConstructor().newInstance();

            invokeSetter(appClass, app, "setApplicationId", String.class, UUID.randomUUID().toString());
            invokeSetter(appClass, app, "setStudentUsername", String.class, username);
            invokeSetter(appClass, app, "setCourseId", String.class, courseId);
            invokeSetter(appClass, app, "setStatement", String.class, statement);

            Class<?> statusEnum = Class.forName("com.group55.ta.model.Application$Status");
            @SuppressWarnings({ "rawtypes", "unchecked" })
            Object pending = Enum.valueOf((Class<? extends Enum>) statusEnum, "PENDING");
            invokeSetter(appClass, app, "setStatus", statusEnum, pending);

            String applyTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            invokeSetter(appClass, app, "setApplyTime", String.class, applyTime);

            Class<?> daoClass = Class.forName("com.group55.ta.dao.ApplicationDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method save = daoClass.getMethod("save", appClass);
            save.invoke(dao, app);
        } catch (ClassNotFoundException e) {
            throw new ServletException("缺少 ApplicationDao/Application（请先合并 Dev-B/Dev-C）", e);
        } catch (Exception e) {
            throw new ServletException("保存申请失败", e);
        }
    }

    private static void invokeSetter(Class<?> clazz, Object target, String method, Class<?> paramType, Object value)
            throws Exception {
        if (value == null) {
            return;
        }
        Method m = clazz.getMethod(method, paramType);
        m.invoke(target, value);
    }
}

