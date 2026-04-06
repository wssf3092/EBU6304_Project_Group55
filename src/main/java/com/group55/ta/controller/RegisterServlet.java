package com.group55.ta.controller;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Registration page and submission handler.
 */
@WebServlet("/auth/register")
public class RegisterServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = sessionUser(request);
        if (user != null) {
            redirect(request, response, homePathFor(user));
            return;
        }
        request.setAttribute("roles", Role.values());
        render(request, response, "auth/register.jsp", "Create Account", "Set up a role-specific account for the recruitment workflow.", "register");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("roles", Role.values());
        request.setAttribute("name", request.getParameter("name"));
        request.setAttribute("email", request.getParameter("email"));
        request.setAttribute("role", request.getParameter("role"));
        try {
            User user = authService.register(
                    request.getParameter("name"),
                    request.getParameter("email"),
                    request.getParameter("password"),
                    request.getParameter("role")
            );
            signIn(request, user);
            redirect(request, response, homePathFor(user));
        } catch (Exception ex) {
            request.setAttribute("formError", ex.getMessage());
            render(request, response, "auth/register.jsp", "Create Account", "Set up a role-specific account for the recruitment workflow.", "register");
        }
    }
}
