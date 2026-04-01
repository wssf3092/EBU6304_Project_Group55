package com.group55.ta.controller;

import java.io.IOException;
import java.util.Optional;

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
        String identifier = trimToNull(request.getParameter("username"));
        String password = trimToNull(request.getParameter("password"));

        if (identifier == null || password == null) {
            request.setAttribute("errorMessage", "邮箱/用户ID 和密码不能为空");
            forwardToView(request, response, "login");
            return;
        }

        UserDao userDao = new UserDao();
        Optional<User> opt = userDao.authenticate(identifier, password);
        if (!opt.isPresent()) {
            request.setAttribute("errorMessage", "邮箱或密码错误");
            request.setAttribute("username", identifier);
            forwardToView(request, response, "login");
            return;
        }

        User user = opt.get();
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);

        // Step 4 起使用 Role.homePath；Step 2 仍统一进入 /dashboard
        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
