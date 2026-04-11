package com.group55.ta.filter;

import com.group55.ta.controller.BaseServlet;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

/**
 * Protects authenticated routes and validates role access.
 */
@WebFilter(urlPatterns = {"/ta/*", "/mo/*", "/admin/*", "/ai/*", "/files/*"})
public class AuthFilter implements Filter {
    private final UserDao userDao = new UserDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        String userId = session == null ? null : (String) session.getAttribute(BaseServlet.CURRENT_USER_ID);
        if (userId == null || userId.trim().isEmpty()) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;
        }

        Optional<User> userOpt = userDao.findById(userId);
        if (!userOpt.isPresent() || !userOpt.get().isActive()) {
            if (session != null) {
                session.invalidate();
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;
        }

        User user = userOpt.get();
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        if (!isAllowed(path, user)) {
            Role role = user.getRoleEnum();
            httpResponse.sendRedirect(httpRequest.getContextPath() + (role == null ? "/auth/login" : role.getHomePath()));
            return;
        }

        httpRequest.setAttribute("currentUser", user);
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String path, User user) {
        if (path.startsWith("/ta/")) {
            return Role.TA.name().equalsIgnoreCase(user.getRole());
        }
        if (path.startsWith("/mo/")) {
            return Role.MO.name().equalsIgnoreCase(user.getRole());
        }
        if (path.startsWith("/admin/")) {
            return Role.ADMIN.name().equalsIgnoreCase(user.getRole());
        }
        if (path.startsWith("/ai/skills-gap")) {
            return Role.TA.name().equalsIgnoreCase(user.getRole());
        }
        if (path.startsWith("/ai/match")) {
            return Role.MO.name().equalsIgnoreCase(user.getRole()) || Role.ADMIN.name().equalsIgnoreCase(user.getRole());
        }
        if (path.startsWith("/ai/workload-balance")) {
            return Role.ADMIN.name().equalsIgnoreCase(user.getRole());
        }
        return true;
    }
}
