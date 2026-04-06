package com.group55.ta.controller;

import com.group55.ta.model.ApplicantProfile;
import com.group55.ta.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * TA profile editor.
 */
@WebServlet("/ta/profile")
public class TAProfileServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        request.setAttribute("profile", recruitmentService.findProfile(user.getUserId()).orElse(recruitmentService.getOrCreateProfile(user)));
        request.setAttribute("skillCatalog", recruitmentService.getSkillCatalog());
        render(request, response, "ta/profile.jsp", "Applicant Profile", "Maintain the profile that supports every application.", "ta-profile");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        try {
            recruitmentService.saveProfile(
                    user,
                    request.getParameter("studentId"),
                    request.getParameter("contactEmail"),
                    request.getParameter("major"),
                    request.getParameter("year"),
                    request.getParameter("skills"),
                    request.getParameter("bio"),
                    request.getParameter("maxHours")
            );
            redirect(request, response, "/ta/profile");
        } catch (Exception ex) {
            ApplicantProfile profile = recruitmentService.getOrCreateProfile(user);
            profile.setStudentId(request.getParameter("studentId"));
            profile.setContactEmail(request.getParameter("contactEmail"));
            profile.setMajor(request.getParameter("major"));
            request.setAttribute("profile", profile);
            request.setAttribute("skillCatalog", recruitmentService.getSkillCatalog());
            request.setAttribute("formError", ex.getMessage());
            render(request, response, "ta/profile.jsp", "Applicant Profile", "Maintain the profile that supports every application.", "ta-profile");
        }
    }
}
