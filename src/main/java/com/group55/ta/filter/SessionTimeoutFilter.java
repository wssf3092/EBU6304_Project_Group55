package com.group55.ta.filter;

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

/**
 * Enforces a soft inactivity timeout on authenticated sessions.
 * <p>
 * Every request through a protected route ({@code /ta/*}, {@code /mo/*}, {@code /admin/*},
 * {@code /ai/*}) updates a {@code lastAccessedMs} timestamp stored in the session.  When
 * the gap between two consecutive requests exceeds {@value #TIMEOUT_MS} ms the session is
 * invalidated and the browser is redirected to the login page with a {@code ?timeout=1}
 * query parameter so the UI can display an appropriate notice.
 * <p>
 * This filter runs <em>before</em> {@link AuthFilter} in the natural servlet ordering (both
 * use {@code @WebFilter}; the container respects declaration order in {@code web.xml} or
 * annotation ordering otherwise).  The timeout value can be overridden at startup via the
 * system property {@code ta.session.timeout.minutes} (default: {@value #DEFAULT_TIMEOUT_MINUTES}).
 */
@WebFilter(urlPatterns = {"/ta/*", "/mo/*", "/admin/*", "/ai/*"})
public class SessionTimeoutFilter implements Filter {

    private static final String LAST_ACCESS_KEY = "sessionLastAccessedMs";
    private static final int DEFAULT_TIMEOUT_MINUTES = 30;
    private static final long TIMEOUT_MS = resolveTimeoutMs();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        if (session != null) {
            Long lastAccess = (Long) session.getAttribute(LAST_ACCESS_KEY);
            long now = System.currentTimeMillis();
            if (lastAccess != null && (now - lastAccess) > TIMEOUT_MS) {
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth/login?timeout=1");
                return;
            }
            session.setAttribute(LAST_ACCESS_KEY, now);
        }

        chain.doFilter(request, response);
    }

    private static long resolveTimeoutMs() {
        try {
            String prop = System.getProperty("ta.session.timeout.minutes");
            if (prop != null && !prop.trim().isEmpty()) {
                int minutes = Integer.parseInt(prop.trim());
                if (minutes > 0) {
                    return (long) minutes * 60_000L;
                }
            }
        } catch (NumberFormatException ignored) {
        }
        return (long) DEFAULT_TIMEOUT_MINUTES * 60_000L;
    }
}
