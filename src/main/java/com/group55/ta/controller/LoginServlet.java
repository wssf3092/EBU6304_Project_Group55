package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Login page and submission handler.
 */
@WebServlet("/auth/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = sessionUser(request);
        if (user != null) {
            redirect(request, response, homePathFor(user));
            return;
        }
        render(request, response, "auth/login.jsp", "Sign In", "Access the TA recruitment workspace.", "login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            User user = authService.authenticate(request.getParameter("email"), request.getParameter("password"));
            signIn(request, user);
            redirect(request, response, homePathFor(user));
        } catch (Exception ex) {
            request.setAttribute("formError", ex.getMessage());
            request.setAttribute("email", request.getParameter("email"));
            render(request, response, "auth/login.jsp", "Sign In", "Access the TA recruitment workspace.", "login");
        }
    }
}
