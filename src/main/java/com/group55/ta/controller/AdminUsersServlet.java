package com.group55.ta.controller;

import com.group55.ta.dao.UserDao;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/users")
public class AdminUsersServlet extends BaseServlet {

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<User> users = userDao.listAll();
        request.setAttribute("users", users);
        request.setAttribute("pageTitle", "用户管理");
        request.setAttribute("activeNav", "admin-users");
        forwardToView(request, response, "admin/users");
    }
}
