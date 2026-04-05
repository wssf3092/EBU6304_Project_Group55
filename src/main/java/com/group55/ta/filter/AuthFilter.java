package com.group55.ta.filter;

import com.group55.ta.controller.BaseServlet;
import com.group55.ta.dao.UserDao;
import com.group55.ta.model.Role;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Protects role-prefixed routes (Step 4).
 */
@WebFilter(urlPatterns = {"/ta/*", "/mo/*", "/admin/*"})
public class AuthFilter implements Filter {

    private final UserDao userDao = new UserDao();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        String userId = session == null ? null : (String) session.getAttribute(BaseServlet.CURRENT_USER_ID);

        if (userId == null || userId.trim().isEmpty()) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login");
            return;
        }

        Optional<User> userOpt = userDao.findById(userId.trim());
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
            String home = role == null ? "/auth/login" : role.getHomePath();
            httpResponse.sendRedirect(httpRequest.getContextPath() + home);
            return;
        }

        httpRequest.setAttribute("currentUser", user);
        chain.doFilter(request, response);
    }

    private boolean isAllowed(String path, User user) {
        Role r = user.getRoleEnum();
        if (path.startsWith("/ta/")) {
            return r == Role.TA;
        }
        if (path.startsWith("/mo/")) {
            return r == Role.MO;
        }
        if (path.startsWith("/admin/")) {
            return r == Role.ADMIN;
        }
        return true;
    }
}
