package com.group55.ta.controller;

import com.group55.ta.model.User;
import com.group55.ta.util.CvFileUtil;
import com.group55.ta.util.FlashUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;

/**
 * CV upload endpoint.
 */
@WebServlet("/ta/cv/upload")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class CVUploadServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        redirect(request, response, "/ta/profile");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = currentUser(request);
        if (user == null) {
            redirect(request, response, "/auth/login");
            return;
        }
        try {
            Part filePart = request.getPart("cvFile");
            String fileName = CvFileUtil.saveUpload(user.getUserId(), filePart);
            recruitmentService.attachCvMetadata(user, fileName);
            FlashUtil.success(request, "CV uploaded successfully.");
        } catch (Exception ex) {
            FlashUtil.error(request, ex.getMessage());
        }
        redirect(request, response, "/ta/profile");
    }
}
