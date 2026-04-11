package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Job application submission handler.
 */
@WebServlet("/ta/jobs/apply")
public class ApplyServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String jobId = request.getParameter("jobId");
        try {
            recruitmentService.applyForJob(user, jobId, request.getParameter("coverLetter"));
            FlashUtil.success(request, "Application submitted.");
            redirect(request, response, "/ta/applications");
        } catch (Exception ex) {
            FlashUtil.error(request, ex.getMessage());
            redirect(request, response, "/ta/jobs?jobId=" + jobId);
        }
    }
}
