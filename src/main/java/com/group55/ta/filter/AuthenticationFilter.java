package com.group55.ta.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to protect authorized routes and validate session status.
 */
@WebFilter(urlPatterns = {"/dashboard", "/course/*", "/courses/*", "/application/*", "/apply", "/applications"})
public class AuthenticationFilter implements Filter {

    // Whitelist paths that bypass authentication checks
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

        // Secure substring to grab relative path
        String path = req.getRequestURI().substring(req.getContextPath().length());

        // 1. Check whitelist or static resources
        if (WHITELIST.contains(path) || path.startsWith("/static/") || path.equals("/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Validate current session and user object
        HttpSession session = req.getSession(false);
        boolean loggedIn = (session != null && session.getAttribute("user") != null);

        if (loggedIn) {
            // User is authenticated, proceed
            chain.doFilter(request, response);
        } else {
            // Unauthenticated request, redirect to login page
            res.sendRedirect(req.getContextPath() + "/login");
        }
    }

    @Override
    public void destroy() {
    }
}
