package com.group55.ta.controller;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = trimToNull(request.getParameter("username"));
        String password = trimToNull(request.getParameter("password"));
        String confirmPassword = trimToNull(request.getParameter("confirmPassword"));
        String email = trimToNull(request.getParameter("email"));
        String fullName = trimToNull(request.getParameter("fullName"));
        String role = trimToNull(request.getParameter("role"));

        if (username == null || password == null || confirmPassword == null) {
            request.setAttribute("errorMessage", "用户名与密码为必填项");
            forwardToView(request, response, "register");
            return;
        }
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "两次输入的密码不一致");
            forwardToView(request, response, "register");
            return;
        }

        if (userExists(username)) {
            request.setAttribute("errorMessage", "用户名已存在");
            forwardToView(request, response, "register");
            return;
        }

        Object user = buildUser(username, password, email, fullName, role);
        saveUser(user);

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean userExists(String username) throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.UserDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method findByUsername = daoClass.getMethod("findByUsername", String.class);
            Object existing = findByUsername.invoke(dao, username);
            return existing != null;
        } catch (ClassNotFoundException e) {
            throw new ServletException("缺少 com.group55.ta.dao.UserDao（请先合并 Dev-C 的 DAO 实现）", e);
        } catch (Exception e) {
            throw new ServletException("检查用户名失败", e);
        }
    }

    private Object buildUser(String username, String password, String email, String fullName, String role)
            throws ServletException {
        try {
            Class<?> userClass = Class.forName("com.group55.ta.model.User");
            Object user = userClass.getDeclaredConstructor().newInstance();

            invokeSetter(userClass, user, "setUsername", String.class, username);
            invokeSetter(userClass, user, "setPassword", String.class, password);
            invokeSetter(userClass, user, "setEmail", String.class, email);
            invokeSetter(userClass, user, "setFullName", String.class, fullName);

            if (role != null) {
                Class<?> roleEnum = Class.forName("com.group55.ta.model.User$Role");
                @SuppressWarnings({ "rawtypes", "unchecked" })
                Object enumValue = Enum.valueOf((Class<? extends Enum>) roleEnum, role);
                invokeSetter(userClass, user, "setRole", roleEnum, enumValue);
            }

            return user;
        } catch (ClassNotFoundException e) {
            throw new ServletException("缺少 com.group55.ta.model.User（请先合并 Dev-B 的 Model 实现）", e);
        } catch (Exception e) {
            throw new ServletException("构建用户对象失败", e);
        }
    }

    private void saveUser(Object user) throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.UserDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method save = daoClass.getMethod("save", Class.forName("com.group55.ta.model.User"));
            save.invoke(dao, user);
        } catch (ClassNotFoundException e) {
            throw new ServletException("缺少 UserDao/User（请先合并 Dev-B/Dev-C）", e);
        } catch (Exception e) {
            throw new ServletException("保存用户失败", e);
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

