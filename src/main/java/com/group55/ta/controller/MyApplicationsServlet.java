package com.group55.ta.controller;

import com.group55.ta.model.Application;
import com.group55.ta.model.User;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TA 查看自己的申请（Step 6 命名）。
 */
@WebServlet("/ta/applications")
public class MyApplicationsServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        List<Application> apps = recruitmentService.listApplicationsEnrichedForApplicant(user.getUserId());
        request.setAttribute("applications", apps);
        request.setAttribute("pageTitle", "我的申请");
        request.setAttribute("activeNav", "ta-applications");
        forwardToView(request, response, "ta/applications");
    }
}
