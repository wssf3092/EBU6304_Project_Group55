package com.group55.ta.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Abstract base class for all Servlet controllers in this application.
 *
 * <p>All Servlet controllers should extend this class and override the
 * {@code doGet} and/or {@code doPost} methods to handle HTTP requests.</p>
 *
 * <p>This class provides common utility methods for:</p>
 * <ul>
 *   <li>Forwarding requests to JSP views under {@code /WEB-INF/views/}</li>
 *   <li>Sending JSON responses (for any AJAX endpoints)</li>
 * </ul>
 *
 * <p><b>MVC Role:</b> Controller (C layer)</p>
 *
 * @author Group 55
 * @version Sprint 0 - skeleton only
 */
public abstract class BaseServlet extends HttpServlet {

    /**
     * Forwards the current request to a JSP view located under
     * {@code /WEB-INF/views/}.
     *
     * @param req      the incoming {@link HttpServletRequest}
     * @param resp     the outgoing {@link HttpServletResponse}
     * @param viewPath the relative path of the JSP file under {@code /WEB-INF/views/},
     *                 e.g. {@code "login.jsp"} resolves to {@code /WEB-INF/views/login.jsp}
     * @throws ServletException if a servlet-specific error occurs during forwarding
     * @throws IOException      if an I/O error occurs during forwarding
     */
    protected void forwardToView(HttpServletRequest req, HttpServletResponse resp, String viewPath)
            throws ServletException, IOException {
        // TODO: Forward request to JSP view under /WEB-INF/views/
        req.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(req, resp);
    }

    /**
     * Sends a JSON string as the HTTP response body with content type
     * {@code application/json; charset=UTF-8}.
     *
     * @param resp the outgoing {@link HttpServletResponse}
     * @param json the JSON string to write to the response
     */
    protected void sendJsonResponse(HttpServletResponse resp, String json) {
        // TODO: Set content type to application/json and write response
    }
}
