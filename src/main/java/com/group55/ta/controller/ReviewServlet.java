package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * MO decision submission handler.
 */
@WebServlet("/mo/applications/review")
public class ReviewServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        String jobId = request.getParameter("jobId");
        try {
            recruitmentService.reviewApplication(
                    user,
                    request.getParameter("applicationId"),
                    request.getParameter("decision"),
                    request.getParameter("note")
            );
            FlashUtil.success(request, "Application decision saved.");
        } catch (Exception ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirect(request, response, "/mo/jobs/applicants?jobId=" + jobId);
    }
}
