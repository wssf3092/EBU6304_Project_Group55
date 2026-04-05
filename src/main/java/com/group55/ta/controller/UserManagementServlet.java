package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin 用户启用/禁用（Step 6）。
 */
@WebServlet("/admin/users")
public class UserManagementServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("users", recruitmentService.listUsersForAdmin());
        request.setAttribute("pageTitle", "用户管理");
        request.setAttribute("activeNav", "admin-users");
        forwardToView(request, response, "admin/users");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User admin = currentUser(request);
        String userId = request.getParameter("userId");
        boolean active = Boolean.parseBoolean(request.getParameter("active"));
        try {
            recruitmentService.setUserActive(admin, userId, active);
            FlashUtil.success(request, "账号状态已更新。");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirect(request, response, "/admin/users");
    }
}
