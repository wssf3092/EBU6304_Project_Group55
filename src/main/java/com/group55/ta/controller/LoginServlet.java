package com.group55.ta.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.User;

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

        UserDao userDao = new UserDao();
        User user = userDao.authenticate(username, password);
        if (user == null) {
            request.setAttribute("errorMessage", "用户名或密码错误");
            forwardToView(request, response, "login");
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        // Role-based redirect (currently all go to /dashboard)
        String role = user.getRole();
        if ("TEACHER".equalsIgnoreCase(role) || "Teacher".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else if ("ADMIN".equalsIgnoreCase(role) || "Admin".equals(role)) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

