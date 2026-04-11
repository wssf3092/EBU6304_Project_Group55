package com.group55.ta.controller;

import com.group55.ta.model.Role;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.group55.ta.model.Role;
import com.group55.ta.service.AuthService;
import com.group55.ta.util.ValidationUtil;

@WebServlet("/auth/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        forwardToView(request, response, "register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String password = trimToNull(request.getParameter("password"));
        String confirmPassword = trimToNull(request.getParameter("confirmPassword"));
        String email = trimToNull(request.getParameter("email"));
        String name = trimToNull(request.getParameter("name"));
        String roleRaw = trimToNull(request.getParameter("role"));

        if (password == null || confirmPassword == null || email == null || name == null) {
            request.setAttribute("errorMessage", "姓名、邮箱与密码为必填项");
            forwardToView(request, response, "register");
            return;
        }
        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "两次输入的密码不一致");
            forwardToView(request, response, "register");
            return;
        }
        if (!ValidationUtil.isValidEmail(email)) {
            request.setAttribute("errorMessage", "邮箱格式不正确");
            forwardToView(request, response, "register");
            return;
        }

        Role role = mapRegistrationRole(roleRaw);
        if (role == null) {
            request.setAttribute("errorMessage", "请选择有效角色");
            forwardToView(request, response, "register");
            return;
        }

        AuthService authService = new AuthService();
        try {
            authService.register(name, email, password, role);
        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("name", name);
            request.setAttribute("role", roleRaw);
            forwardToView(request, response, "register");
            return;
        } catch (IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("email", email);
            request.setAttribute("name", name);
            request.setAttribute("role", roleRaw);
            forwardToView(request, response, "register");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }

    private static Role mapRegistrationRole(String roleRaw) {
        if (roleRaw == null) {
            return Role.TA;
        }
        if ("Student".equalsIgnoreCase(roleRaw) || "TA".equalsIgnoreCase(roleRaw)) {
            return Role.TA;
        }
        if ("Teacher".equalsIgnoreCase(roleRaw) || "MO".equalsIgnoreCase(roleRaw)) {
            return Role.MO;
        }
        if ("Admin".equalsIgnoreCase(roleRaw) || "ADMIN".equalsIgnoreCase(roleRaw)) {
            return Role.ADMIN;
        }
        return Role.fromString(roleRaw);
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
