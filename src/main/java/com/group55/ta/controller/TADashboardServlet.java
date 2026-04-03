package com.group55.ta.controller;

import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.group55.ta.model.Application;

/**
 * TA workspace home (applications overview).
 */
@WebServlet("/ta/dashboard")
public class TADashboardServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("rolePrefix", "/ta");
        request.setAttribute("dashboardMode", "ta");
        List<Application> apps = recruitmentService.listApplicationsEnrichedForApplicant(user.getUserId());
        request.setAttribute("applications", apps);
        forwardToView(request, response, "dashboard");
    }
}
