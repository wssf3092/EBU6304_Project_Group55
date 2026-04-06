package com.group55.ta.controller;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.service.AiService;
import com.group55.ta.service.AuthService;
import com.group55.ta.service.RecruitmentService;
import com.group55.ta.util.FlashUtil;
import com.group55.ta.util.JsonResponseUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Shared servlet helpers.
 */
public abstract class BaseServlet extends HttpServlet {
    public static final String CURRENT_USER_ID = "currentUserId";

    protected final AuthService authService = new AuthService();
    protected final RecruitmentService recruitmentService = new RecruitmentService();
    protected final AiService aiService = new AiService();

    protected void render(HttpServletRequest request,
                          HttpServletResponse response,
                          String viewPath,
                          String pageTitle,
                          String pageSubtitle,
                          String activeNav) throws ServletException, IOException {
        FlashUtil.expose(request);
        request.setAttribute("pageTitle", pageTitle);
        request.setAttribute("pageSubtitle", pageSubtitle);
        request.setAttribute("activeNav", activeNav);
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String relativePath) throws IOException {
        response.sendRedirect(request.getContextPath() + relativePath);
    }

    protected void signIn(HttpServletRequest request, User user) {
        request.getSession(true).setAttribute(CURRENT_USER_ID, user.getUserId());
    }

    protected void signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    protected User currentUser(HttpServletRequest request) {
        Object value = request.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    protected User sessionUser(HttpServletRequest request) {
        User user = currentUser(request);
        if (user != null) {
            return user;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute(CURRENT_USER_ID);
        if (!(value instanceof String)) {
            return null;
        }
        return authService.findById((String) value).orElse(null);
    }

    protected void json(HttpServletResponse response, Object payload) throws IOException {
        JsonResponseUtil.write(response, payload);
    }

    protected String homePathFor(User user) {
        Role role = user == null ? null : user.getRoleEnum();
        return role == null ? "/auth/login" : role.getHomePath();
    }
}
