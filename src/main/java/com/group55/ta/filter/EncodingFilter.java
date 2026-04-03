package com.group55.ta.filter;

import javax.servlet.*;
import java.io.IOException;

/**
 * Filter to enforce UTF-8 encoding to solve garbled character issues.
 * Applies to all requests. Mapped in {@code web.xml} (Step 4).
 */
public class EncodingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logic if necessary
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        // Set proper encoding for receiving and sending data
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // Pass the request along the filter chain
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Cleanup logic if necessary
    }
}
