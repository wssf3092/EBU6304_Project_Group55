package com.group55.ta.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.User;

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

        UserDao userDao = new UserDao();
        if (userDao.findByUsername(username) != null) {
            request.setAttribute("errorMessage", "用户名已存在");
            forwardToView(request, response, "register");
            return;
        }

        // Default role to "Student" if not provided
        if (role == null) {
            role = "Student";
        }
        // Constructor order: username, password, role, fullName, email
        User user = new User(username, password, role,
                fullName != null ? fullName : username,
                email != null ? email : "");
        userDao.save(user);

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}

