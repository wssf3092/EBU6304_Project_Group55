package com.group55.ta.controller;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;
import com.group55.ta.service.AuthService;
import com.group55.ta.service.RecruitmentService;
import com.group55.ta.util.FlashUtil;
import com.group55.ta.util.GsonProvider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Shared servlet helpers (Step 4).
 */
public abstract class BaseServlet extends HttpServlet {

    /** Session key for signed-in user id (replaces storing full {@link User}). */
    public static final String CURRENT_USER_ID = "CURRENT_USER_ID";

    protected transient AuthService authService;
    protected transient RecruitmentService recruitmentService;

    @Override
    public void init() throws ServletException {
        super.init();
        authService = new AuthService();
        recruitmentService = new RecruitmentService();
    }

    protected void render(HttpServletRequest request, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        forwardToView(request, response, viewName);
    }

    protected void forwardToView(HttpServletRequest request, HttpServletResponse response, String viewName)
            throws ServletException, IOException {
        FlashUtil.expose(request);
        String name = viewName;
        if (!name.endsWith(".jsp")) {
            name = name + ".jsp";
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/views/" + name);
        dispatcher.forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /**
     * User set by {@link com.group55.ta.filter.AuthFilter} on protected routes.
     */
    protected User currentUser(HttpServletRequest request) {
        Object v = request.getAttribute("currentUser");
        return v instanceof User ? (User) v : null;
    }

    /**
     * Resolves user from request attribute (filter) or session id + {@link AuthService}.
     */
    protected User sessionUser(HttpServletRequest request) {
        User fromFilter = currentUser(request);
        if (fromFilter != null) {
            return fromFilter;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute(CURRENT_USER_ID);
        if (!(id instanceof String)) {
            return null;
        }
        return authService.findById((String) id).orElse(null);
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

    protected Optional<User> findSessionUser(HttpServletRequest request) {
        return Optional.ofNullable(sessionUser(request));
    }

    protected String homePathFor(User user) {
        Role role = user == null ? null : user.getRoleEnum();
        return role == null ? "/auth/login" : role.getHomePath();
    }

    protected void json(HttpServletResponse response, Object payload) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(GsonProvider.gson().toJson(payload));
            out.flush();
        }
    }

    protected void sendJsonResponse(HttpServletResponse response, String jsonBody) throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            out.write(jsonBody == null ? "null" : jsonBody);
            out.flush();
        }
    }
}
