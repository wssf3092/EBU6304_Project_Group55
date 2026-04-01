package com.group55.ta.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.group55.ta.model.Role;
import com.group55.ta.model.User;

/**
 * Filter to protect authorized routes and validate session status.
 */
public class AuthenticationFilter implements Filter {

    private static final List<String> WHITELIST = Arrays.asList(
            "/login",
            "/register",
            "/index.jsp"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI().substring(req.getContextPath().length());

        if (WHITELIST.contains(path) || path.startsWith("/static/") || path.equals("/")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (loggedIn) {
            User user = (User) session.getAttribute("user");
            Role roleEnum = user != null ? user.getRoleEnum() : null;
            String role = (user != null && user.getRole() != null) ? user.getRole() : "";

            if (path.startsWith("/courses/manage") || path.startsWith("/courses/new")) {
                if (!isMo(roleEnum, role)) {
                    res.sendRedirect(req.getContextPath() + "/dashboard");
                    return;
                }
            }

            if (path.equals("/apply") || path.startsWith("/apply")) {
                if (!isTa(roleEnum, role)) {
                    res.sendRedirect(req.getContextPath() + "/dashboard");
                    return;
                }
            }

            chain.doFilter(request, response);
        } else {
            res.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private static boolean isMo(Role roleEnum, String role) {
        return roleEnum == Role.MO
                || "MO".equalsIgnoreCase(role)
                || "TEACHER".equalsIgnoreCase(role)
                || "Teacher".equals(role);
    }

    private static boolean isTa(Role roleEnum, String role) {
        return roleEnum == Role.TA
                || "TA".equalsIgnoreCase(role)
                || "STUDENT".equalsIgnoreCase(role)
                || "Student".equals(role);
    }

    @Override
    public void destroy() {
    }
}
