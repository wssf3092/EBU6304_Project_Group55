package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * AJAX endpoint for applicant match analysis.
 */
@WebServlet("/ai/match")
public class AIMatchServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        try {
            json(response, aiService.buildMatchInsight(user, request.getParameter("applicationId")));
        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            json(response, errorPayload(ex.getMessage()));
        }
    }

    private static Object errorPayload(String message) {
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("available", false);
        payload.put("error", message);
        return payload;
    }
}
