package com.group55.ta.controller;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = trimToNull(request.getParameter("username"));
        String password = trimToNull(request.getParameter("password"));

        if (username == null || password == null) {
            request.setAttribute("errorMessage", "用户名和密码不能为空");
            forwardToView(request, response, "login");
            return;
        }

        Object user = authenticate(username, password);
        if (user == null) {
            request.setAttribute("errorMessage", "用户名或密码错误");
            forwardToView(request, response, "login");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("currentUser", user);
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private Object authenticate(String username, String password) throws ServletException {
        try {
            Class<?> daoClass = Class.forName("com.group55.ta.dao.UserDao");
            Object dao = daoClass.getDeclaredConstructor().newInstance();
            Method authenticate = daoClass.getMethod("authenticate", String.class, String.class);
            return authenticate.invoke(dao, username, password);
        } catch (ClassNotFoundException e) {
            throw new ServletException("缺少 com.group55.ta.dao.UserDao（请先合并 Dev-C 的 DAO 实现）", e);
        } catch (Exception e) {
            throw new ServletException("登录认证失败", e);
        }
    }
}

