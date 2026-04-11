package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Admin user management page.
 */
@WebServlet("/admin/users")
public class UserManagementServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("users", recruitmentService.listUsersForAdmin());
        render(request, response, "admin/users.jsp", "User Management", "Control account status and inspect operational readiness.", "admin-users");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        try {
            recruitmentService.setUserActive(user, request.getParameter("userId"), Boolean.parseBoolean(request.getParameter("active")));
            FlashUtil.success(request, "Account status updated.");
        } catch (Exception ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirect(request, response, "/admin/users");
    }
}
