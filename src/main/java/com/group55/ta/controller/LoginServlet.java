package com.group55.ta.controller;

import com.group55.ta.model.User;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/auth/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User existing = sessionUser(request);
        if (existing != null) {
            redirect(request, response, homePathFor(existing));
            return;
        }
        request.setAttribute("pageTitle", "登录");
        forwardToView(request, response, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("pageTitle", "登录");
        String identifier = trimToNull(request.getParameter("username"));
        String password = trimToNull(request.getParameter("password"));

        if (identifier == null || password == null) {
            request.setAttribute("errorMessage", "邮箱/用户ID 和密码不能为空");
            forwardToView(request, response, "auth/login");
            return;
        }

        try {
            User user = authService.authenticate(identifier, password);
            signIn(request, user);
            redirect(request, response, homePathFor(user));
        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("username", identifier);
            forwardToView(request, response, "auth/login");
        } catch (IllegalStateException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
            request.setAttribute("username", identifier);
            forwardToView(request, response, "auth/login");
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
