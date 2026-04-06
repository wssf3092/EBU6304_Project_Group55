package com.group55.ta.controller;

import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TA application history page.
 */
@WebServlet("/ta/applications")
public class MyApplicationsServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("applications", recruitmentService.listApplicationsForApplicant(user.getUserId()));
        render(request, response, "ta/applications.jsp", "Application Status", "Track every submission and review outcome in one place.", "ta-applications");
    }
}
